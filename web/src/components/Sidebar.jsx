import { useState } from 'react'
import { Search, Settings, LogOut, MessageSquare, UserPlus, Check, X } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function Sidebar({ contacts, activeChatUser, onSelectContact, currentUser, pendingRequests, onAcceptRequest, onRejectRequest, onSendRequest }) {
    const [searchQuery, setSearchQuery] = useState('')
    const [showAddFriend, setShowAddFriend] = useState(false)
    const [friendEmail, setFriendEmail] = useState('')
    const [requestMsg, setRequestMsg] = useState('')

    const handleAddFriend = async (e) => {
        e.preventDefault()
        setRequestMsg('')
        if (!friendEmail.trim()) return

        const result = await onSendRequest(friendEmail.trim())
        if (result.success) {
            setRequestMsg('Request sent!')
            setFriendEmail('')
            setTimeout(() => { setShowAddFriend(false); setRequestMsg(''); }, 2000)
        } else {
            setRequestMsg(result.error)
        }
    }

    const handleLogout = async () => {
        await supabase.auth.signOut()
    }

    const filteredContacts = contacts?.filter(contact =>
        (contact.username || contact.email || '').toLowerCase().includes(searchQuery.toLowerCase())
    ) || []

    return (
        <div className="sidebar">
            <div className="sidebar-header glass-panel" style={{ border: 'none', borderRadius: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <div className="user-avatar" style={{ width: 36, height: 36, fontSize: '1rem', background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)' }}>
                        {(currentUser.email || 'U')[0].toUpperCase()}
                    </div>
                    <span className="sidebar-title">Chats</span>
                </div>
                <div style={{ display: 'flex', gap: '0.25rem' }}>
                    <button className="icon-btn" title="Add Friend" onClick={() => setShowAddFriend(!showAddFriend)} style={{ color: 'var(--text-muted)' }}>
                        <UserPlus size={20} />
                    </button>
                    <button className="icon-btn" title="Settings" style={{ color: 'var(--text-muted)' }}>
                        <Settings size={20} />
                    </button>
                    <button className="icon-btn" title="Log Out" onClick={handleLogout} style={{ color: 'var(--text-muted)' }}>
                        <LogOut size={20} />
                    </button>
                </div>
            </div>

            {showAddFriend && (
                <div style={{ padding: '1rem', borderBottom: '1px solid var(--border-color)', background: 'rgba(255,255,255,0.02)' }}>
                    <form onSubmit={handleAddFriend} style={{ display: 'flex', gap: '0.5rem', flexDirection: 'column' }}>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <input
                                type="email"
                                className="input-field"
                                placeholder="Friend's email address..."
                                value={friendEmail}
                                onChange={(e) => setFriendEmail(e.target.value)}
                                style={{ flex: 1, padding: '0.5rem 1rem', fontSize: '0.9rem' }}
                                required
                            />
                            <button type="submit" className="btn-primary" style={{ padding: '0.5rem 1rem', width: 'auto' }}>Send</button>
                        </div>
                        {requestMsg && <div style={{ fontSize: '0.8rem', color: requestMsg.includes('sent') ? '#4ade80' : '#f87171' }}>{requestMsg}</div>}
                    </form>
                </div>
            )}

            <div className="search-bar-container">
                <div className="search-input-wrapper">
                    <Search className="search-icon" size={18} />
                    <input
                        type="text"
                        className="input-field search-input"
                        placeholder="Search or start new chat"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
            </div>

            <div className="users-list custom-scrollbar">
                {/* Pending Requests Section */}
                {pendingRequests && pendingRequests.length > 0 && (
                    <div style={{ marginBottom: '1rem' }}>
                        <div style={{ padding: '0.5rem 1rem', fontSize: '0.8rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 'bold', letterSpacing: '1px' }}>
                            Pending Requests
                        </div>
                        {pendingRequests.map(req => (
                            <div key={req.id} className="user-item glass-panel" style={{ margin: '0.25rem 0.5rem', borderRadius: '0.5rem', padding: '0.75rem' }}>
                                <div className="user-avatar" style={{ width: 32, height: 32, fontSize: '0.9rem' }}>
                                    {(req.sender?.username || req.sender?.email || 'U')[0].toUpperCase()}
                                </div>
                                <div className="user-info" style={{ flex: 1, overflow: 'hidden' }}>
                                    <div className="user-name" style={{ fontSize: '0.9rem' }}>{req.sender?.username || req.sender?.email}</div>
                                    <div className="user-status" style={{ fontSize: '0.8rem' }}>wants to connect</div>
                                </div>
                                <div style={{ display: 'flex', gap: '0.25rem' }}>
                                    <button onClick={(e) => { e.stopPropagation(); onAcceptRequest(req.id); }} style={{ background: '#10b981', color: 'white', border: 'none', borderRadius: '50%', width: 28, height: 28, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}>
                                        <Check size={16} />
                                    </button>
                                    <button onClick={(e) => { e.stopPropagation(); onRejectRequest(req.id); }} style={{ background: '#ef4444', color: 'white', border: 'none', borderRadius: '50%', width: 28, height: 28, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}>
                                        <X size={16} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                <div style={{ padding: '0.5rem 1rem', fontSize: '0.8rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 'bold', letterSpacing: '1px' }}>
                    Chats
                </div>
                {filteredContacts.length === 0 ? (
                    <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        <MessageSquare size={32} style={{ margin: '0 auto 1rem', opacity: 0.5 }} />
                        <p>No contacts found.</p>
                    </div>
                ) : (
                    filteredContacts.map(contact => (
                        <div
                            key={contact.id}
                            className={`user-item ${activeChatUser?.id === contact.id ? 'active glass-panel' : ''}`}
                            onClick={() => onSelectContact(contact)}
                            style={activeChatUser?.id === contact.id ? { borderRadius: '0.5rem', margin: '0.25rem 0.5rem' } : {}}
                        >
                            <div className="user-avatar">
                                {(contact.username || contact.email || 'U')[0].toUpperCase()}
                            </div>
                            <div className="user-info">
                                <div className="user-name">{contact.username || contact.email}</div>
                                <div className="user-status">Hey there! I am using Chatzz.</div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    )
}
