import { Send, MoreVertical, Paperclip, Smile, Image as ImageIcon, Search, MessageSquare, UserPlus, Check, CheckCheck } from 'lucide-react'
import EmojiPicker from 'emoji-picker-react'
import { supabase } from '../supabaseClient'

export default function ChatArea({ chatId, chatUser, currentUser }) {
    const [messages, setMessages] = useState([])
    const [newMessage, setNewMessage] = useState('')
    const [showEmojiPicker, setShowEmojiPicker] = useState(false)
    const messagesEndRef = useRef(null)

    useEffect(() => {
        if (chatId) {
            fetchMessages()
            const subscription = supabase
                .channel(`messages:${chatId}`)
                // Listen formatting is specific for supabase v2
                .on('postgres_changes', { event: 'INSERT', schema: 'public', table: 'messages', filter: `chat_id=eq.${chatId}` }, payload => {
                    setMessages(current => [...current, payload.new])
                    if (payload.new.sender_id !== currentUser.id) {
                        markMessagesAsRead([payload.new])
                    }
                })
                .on('postgres_changes', { event: 'UPDATE', schema: 'public', table: 'messages', filter: `chat_id=eq.${chatId}` }, payload => {
                    setMessages(current => current.map(msg => msg.id === payload.new.id ? payload.new : msg))
                })
                .subscribe()

            return () => {
                supabase.removeChannel(subscription)
            }
        } else {
            setMessages([])
        }
    }, [chatId])

    useEffect(() => {
        scrollToBottom()
    }, [messages])



    const markMessagesAsRead = async (msgs) => {
        if (!currentUser) return;
        const unreadMsgIds = msgs
            .filter(m => m.sender_id !== currentUser.id && !m.read_at)
            .map(m => m.id)

        if (unreadMsgIds.length > 0) {
            await supabase
                .from('messages')
                .update({ read_at: new Date().toISOString() })
                .in('id', unreadMsgIds)
        }
    }

    const fetchMessages = async () => {
        try {
            // Make sure you have a messages table with chat_id column in Supabase
            const { data, error } = await supabase
                .from('messages')
                .select('*')
                .eq('chat_id', chatId)
                .order('created_at', { ascending: true })

            if (error && error.code !== 'PGRST116') {
                // PGRST116 is just no rows returned
                console.error('Error fetching messages:', error)
            }

            if (data) {
                setMessages(data)
                markMessagesAsRead(data)
            } else {
                setMessages([])
            }
        } catch (error) {
            console.error('Exception fetching messages:', error)
        }
    }

    const handleSendMessage = async (e) => {
        e.preventDefault()
        if (!newMessage.trim() || !chatId) return

        const messageText = newMessage
        setNewMessage('') // Optimistic clear

        try {
            const { error } = await supabase
                .from('messages')
                .insert([{
                    chat_id: chatId,
                    sender_id: currentUser.id,
                    text: messageText,
                    // Note: Supabase created_at is handled by the DB default usually, but providing it is fine
                }])

            if (error) {
                console.error('Error sending message:', error)
                setNewMessage(messageText) // Restore on error
            }
        } catch (error) {
            console.error('Exception sending message:', error)
        }
    }

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
    }

    const formatTime = (isoString) => {
        if (!isoString) return ''
        const date = new Date(isoString)
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }

    if (!chatUser) {
        return (
            <div className="empty-chat-state">
                <div className="icon" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '50%', marginBottom: '2rem' }}>
                    <div style={{
                        width: '120px',
                        height: '120px',
                        background: 'linear-gradient(135deg, var(--accent-primary), #60a5fa)',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: 'var(--shadow-glow)'
                    }}>
                        <MessageSquare size={60} color="white" />
                    </div>
                </div>
                <h2 style={{ fontSize: '2rem', marginBottom: '1rem' }}>Chatzz Web</h2>
                <p style={{ fontSize: '1.2rem', color: 'var(--text-muted)', maxWidth: '400px', textAlign: 'center', lineHeight: '1.6' }}>
                    Send and receive messages without keeping your phone online. <br />
                    Select a contact from the sidebar to start chatting.
                </p>
                <div style={{ marginTop: '3rem', padding: '1rem 2rem', background: 'var(--glass-bg)', borderRadius: '2rem', display: 'flex', gap: '1rem', alignItems: 'center', border: '1px solid var(--glass-border)' }}>
                    <span style={{ display: 'inline-block', width: 10, height: 10, background: '#10b981', borderRadius: '50%', boxShadow: '0 0 10px #10b981' }}></span>
                    <span style={{ fontSize: '1rem', fontWeight: '500' }}>End-to-end encrypted</span>
                </div>
            </div>
        )
    }

    const isPending = chatUser.requestStatus === 'pending_outgoing'

    return (
        <div className="chat-area">
            <div className="chat-overlay"></div>

            <div className="chat-header glass-panel" style={{ border: 'none', borderRadius: 0, borderBottom: '1px solid var(--border-color)' }}>
                <div className="user-avatar" style={{ width: 40, height: 40, fontSize: '1.2rem', marginRight: '1rem' }}>
                    {chatUser?.username?.[0]?.toUpperCase() || chatUser?.email?.[0]?.toUpperCase() || 'U'}
                </div>
                <div className="chat-header-info">
                    <div className="chat-header-name">{chatUser?.username || chatUser?.email || 'Unknown User'}</div>
                    <div className="chat-header-status">
                        {isPending ? 'Pending Request' : (
                            chatUser?.last_seen && (new Date() - new Date(chatUser.last_seen)) < 5 * 60 * 1000 ?
                                <span style={{ color: '#10b981', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                    <div style={{ width: 8, height: 8, background: '#10b981', borderRadius: '50%' }}></div> Online
                                </span> : 'Offline'
                        )}
                    </div>
                </div>
                {!isPending && (
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button className="icon-btn"><Search size={20} /></button>
                        <button className="icon-btn"><MoreVertical size={20} /></button>
                    </div>
                )}
            </div>

            {isPending ? (
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '2rem' }}>
                    <div style={{ background: 'var(--glass-bg)', padding: '2rem', borderRadius: '1rem', border: '1px solid var(--glass-border)' }}>
                        <UserPlus size={48} style={{ color: 'var(--accent-primary)', marginBottom: '1rem' }} />
                        <h3 style={{ marginBottom: '0.5rem' }}>Friend Request Sent</h3>
                        <p style={{ color: 'var(--text-muted)' }}>Waiting for {chatUser?.username || chatUser?.email} to accept your request before you can start chatting.</p>
                    </div>
                </div>
            ) : (
                <>
                    <div className="messages-container custom-scrollbar">
                        {messages.map((msg, index) => {
                            const isSent = msg.sender_id === currentUser.id
                            return (
                                <div key={msg.id || index} className={`message-wrapper ${isSent ? 'sent' : 'received'}`}>
                                    <div className="message-bubble">
                                        {msg.text}
                                    </div>
                                    <div className="message-time" style={{ display: 'flex', alignItems: 'center', gap: '4px', justifyContent: 'flex-end', marginTop: '4px' }}>
                                        <span>{formatTime(msg.created_at)}</span>
                                        {isSent && (
                                            msg.read_at ?
                                                <CheckCheck size={14} color="#3b82f6" /> :
                                                <Check size={14} color="gray" />
                                        )}
                                    </div>
                                </div>
                            )
                        })}
                        <div ref={messagesEndRef} />
                    </div>

                    <div style={{ position: 'relative' }}>
                        {showEmojiPicker && (
                            <div style={{ position: 'absolute', bottom: '10px', left: '10px', zIndex: 1000 }}>
                                <EmojiPicker onEmojiClick={(emojiData) => setNewMessage(prev => prev + emojiData.emoji)} theme="dark" autoFocusSearch={false} />
                            </div>
                        )}
                    </div>
                    <form className="chat-input-area glass-panel" style={{ border: 'none', borderRadius: 0, borderTop: '1px solid var(--border-color)' }} onSubmit={handleSendMessage}>
                        <button type="button" className="icon-btn" onClick={() => setShowEmojiPicker(prev => !prev)}><Smile size={24} /></button>
                        <button type="button" className="icon-btn"><Paperclip size={24} /></button>
                        <input
                            type="text"
                            className="msg-input"
                            placeholder="Type a message..."
                            value={newMessage}
                            onChange={(e) => setNewMessage(e.target.value)}
                        />
                        <button type="submit" className="send-btn" disabled={!newMessage.trim()}>
                            <Send size={20} style={{ marginLeft: '2px' }} />
                        </button>
                    </form>
                </>
            )}
        </div>
    )
}
