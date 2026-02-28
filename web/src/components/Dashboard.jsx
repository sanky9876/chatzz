import { useState, useEffect } from 'react'
import Sidebar from './Sidebar'
import ChatArea from './ChatArea'
import { supabase } from '../supabaseClient'

// Simple UUID generator for browser
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

export default function Dashboard({ session }) {
    const [activeChatId, setActiveChatId] = useState(null)
    const [activeChatUser, setActiveChatUser] = useState(null)
    const [contacts, setContacts] = useState([])
    const [pendingRequests, setPendingRequests] = useState([])
    const [outgoingRequests, setOutgoingRequests] = useState([])
    const currentUser = session.user

    useEffect(() => {
        fetchContactsAndRequests()

        // Subscribe to real-time changes on friend_requests
        const subscription = supabase
            .channel('friend_requests_changes')
            .on('postgres_changes', { event: '*', schema: 'public', table: 'friend_requests' }, () => {
                fetchContactsAndRequests() // Refresh on any change
            })
            .subscribe()

        return () => supabase.removeChannel(subscription)
    }, [])

    const fetchContactsAndRequests = async () => {
        try {
            // Fetch all users to map IDs to emails/usernames
            const { data: usersData, error: usersError } = await supabase.from('users').select('*')
            if (usersError) throw usersError

            // Map for quick lookup
            const usersMap = {}
            if (usersData) {
                usersData.forEach(u => usersMap[u.id] = u)
            }

            // Fetch friend requests relevant to the current user
            const { data: reqData, error: reqError } = await supabase
                .from('friend_requests')
                .select('*')

            if (reqError) throw reqError

            const pending = []
            const outgoing = []
            const acceptedFriendsList = []

            if (reqData) {
                reqData.forEach(req => {
                    // If it's incoming and pending
                    if (req.receiver_id === currentUser.id && req.status === 'pending') {
                        pending.push({ ...req, sender: usersMap[req.sender_id] })
                    }
                    // If it's outgoing
                    else if (req.sender_id === currentUser.id) {
                        if (req.status === 'pending') {
                            outgoing.push({ ...req, receiver: usersMap[req.receiver_id] })
                            // Show pending outgoing in the contacts list, but mark it
                            const friendUser = usersMap[req.receiver_id]
                            if (friendUser) acceptedFriendsList.push({ ...friendUser, requestStatus: 'pending_outgoing' })
                        } else if (req.status === 'accepted') {
                            const friendUser = usersMap[req.receiver_id]
                            if (friendUser) acceptedFriendsList.push({ ...friendUser, requestStatus: 'accepted' })
                        }
                    }
                    // If it's incoming and accepted
                    else if (req.receiver_id === currentUser.id && req.status === 'accepted') {
                        const friendUser = usersMap[req.sender_id]
                        if (friendUser) acceptedFriendsList.push({ ...friendUser, requestStatus: 'accepted' })
                    }
                })
            }

            setPendingRequests(pending)
            setOutgoingRequests(outgoing)
            setContacts(acceptedFriendsList)

        } catch (error) {
            console.error('Error fetching contacts:', error.message)
        }
    }

    const handleSendRequest = async (email) => {
        try {
            // Find user by email. Note: the `users` table doesn't have an email column yet, so we search auth.users if possible.
            // Wait, we need an email column in `public.users`. Let's assume there is one, or we search by name?
            // Actually, we should call a Supabase RPC or update the schema to include email in public.users.
            // Let's modify handle_new_user block in schema.sql to add email, and then use that here.

            // For now, let's query the `users` table by `email` assuming we will add it.
            const { data: targetUser, error: searchError } = await supabase
                .from('users')
                .select('*')
                .eq('email', email)
                .single()

            if (searchError || !targetUser) {
                console.error("Search error:", searchError);
                return { success: false, error: 'User not found' }
            }
            if (targetUser.id === currentUser.id) return { success: false, error: 'Cannot add yourself' }

            // Check if request already exists
            const { data: existing } = await supabase
                .from('friend_requests')
                .select('*')
                .or(`and(sender_id.eq.${currentUser.id},receiver_id.eq.${targetUser.id}),and(sender_id.eq.${targetUser.id},receiver_id.eq.${currentUser.id})`)

            if (existing && existing.length > 0) {
                return { success: false, error: 'Request/Friendship already exists' }
            }

            // Send request
            const { error: insertError } = await supabase
                .from('friend_requests')
                .insert([{ sender_id: currentUser.id, receiver_id: targetUser.id, status: 'pending' }])

            if (insertError) throw insertError

            fetchContactsAndRequests()
            return { success: true }
        } catch (error) {
            console.error('Error sending request', error)
            return { success: false, error: 'Failed to send request' }
        }
    }

    const handleAcceptRequest = async (requestId) => {
        const { error } = await supabase
            .from('friend_requests')
            .update({ status: 'accepted' })
            .eq('id', requestId)
        if (!error) fetchContactsAndRequests()
    }

    const handleRejectRequest = async (requestId) => {
        const { error } = await supabase
            .from('friend_requests')
            .update({ status: 'rejected' })
            .eq('id', requestId)
        if (!error) fetchContactsAndRequests()
    }

    const handleSelectContact = async (contact) => {
        setActiveChatUser(contact)

        // We need to either find an existing chat between these two users or create one.
        // First check if they share a chat
        try {
            const { data: memberData, error: memberError } = await supabase
                .from('chat_members')
                .select('chat_id')
                .eq('user_id', currentUser.id)

            if (memberError) throw memberError

            let existingChatId = null;

            if (memberData && memberData.length > 0) {
                const chatIds = memberData.map(m => m.chat_id)
                // Find if contact is in any of these chats
                const { data: sharedData, error: sharedError } = await supabase
                    .from('chat_members')
                    .select('chat_id')
                    .eq('user_id', contact.id)
                    .in('chat_id', chatIds)

                if (!sharedError && sharedData && sharedData.length > 0) {
                    existingChatId = sharedData[0].chat_id
                }
            }

            if (existingChatId) {
                setActiveChatId(existingChatId)
            } else {
                // Create a new chat with an explicit UUID to bypass Supabase RLS generator blocks
                const newChatId = generateUUID()
                const { data: newChat, error: chatError } = await supabase
                    .from('chats')
                    .insert([{ id: newChatId }])
                    .select()
                    .single()

                if (chatError) throw chatError

                // Add both members
                await supabase.from('chat_members').insert([
                    { chat_id: newChat.id, user_id: currentUser.id },
                    { chat_id: newChat.id, user_id: contact.id }
                ])

                setActiveChatId(newChat.id)
            }

        } catch (error) {
            console.error('Error opening chat:', error)
        }
    }

    return (
        <div className="dashboard">
            <Sidebar
                contacts={contacts}
                activeChatUser={activeChatUser}
                onSelectContact={handleSelectContact}
                currentUser={currentUser}
                pendingRequests={pendingRequests}
                onAcceptRequest={handleAcceptRequest}
                onRejectRequest={handleRejectRequest}
                onSendRequest={handleSendRequest}
            />
            <ChatArea
                chatId={activeChatId}
                chatUser={activeChatUser}
                currentUser={currentUser}
            />
        </div>
    )
}
