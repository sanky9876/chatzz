import { useState } from 'react'
import { supabase } from '../supabaseClient'
import { Mail, ArrowRight, Loader2, CheckCircle2 } from 'lucide-react'

export default function Auth() {
    const [loading, setLoading] = useState(false)
    const [email, setEmail] = useState('')
    const [otp, setOtp] = useState('')
    const [message, setMessage] = useState('')
    const [isSuccess, setIsSuccess] = useState(false)
    const [isOtpSent, setIsOtpSent] = useState(false)
    const [isLoginView, setIsLoginView] = useState(true)
    const [password, setPassword] = useState('')

    const handleAuth = async (event) => {
        event.preventDefault()
        setLoading(true)
        setMessage('')
        setIsSuccess(false)

        const handleSendOtp = (e) => {
            e.preventDefault();
            setMessage('');
            if (!email) {
                setMessage('Please enter your email address');
                return;
            }
            setLoading(true);
            // Simulate sending OTP
            setTimeout(() => {
                setIsOtpSent(true);
                setLoading(false);
                setMessage('OTP sent to your email (Demo: Use 123456)');
            }, 500);
        }

        const handleVerifyOtp = async (event) => {
            event.preventDefault()
            setLoading(true)
            setMessage('')
            setIsSuccess(false)

            if (isLoginView) {
                // Real password login
                const { error: signInError } = await supabase.auth.signInWithPassword({
                    email,
                    password: password
                })

                if (signInError) {
                    setMessage(signInError.message)
                } else {
                    setMessage('Logged in successfully!')
                    setIsSuccess(true)
                }
            } else {
                if (otp === "123456") {
                    if (!password || password.length < 6) {
                        setMessage('Password must be at least 6 characters')
                        setLoading(false)
                        return
                    }
                    const { error: signUpError } = await supabase.auth.signUp({
                        email,
                        password: password,
                    })

                    if (signUpError) {
                        setMessage(signUpError.message)
                    } else {
                        setMessage('Account verified successfully! You can now log in.')
                        setIsSuccess(true)
                        setIsLoginView(true)
                        setIsOtpSent(false)
                        setOtp('')
                        // Keep the password they just entered so it's ready for login
                    }
                } else {
                    setMessage('Invalid Demo OTP! Please enter 123456')
                }
            }

            setLoading(false)
        }

        setLoading(false)
    }

    return (
        <div className="auth-container">
            <div className="auth-card glass-panel">
                <div className="auth-logo">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                </div>

                <h1 className="auth-title text-gradient">
                    {isLoginView ? 'Welcome Back' : 'Create Account'}
                </h1>
                <p className="auth-subtitle">
                    {isLoginView ? 'Enter your email and password' : 'Enter your email to connect with friends'}
                </p>

                <form className="auth-form" onSubmit={isOtpSent ? handleVerifyOtp : handleSendOtp}>
                    {!isOtpSent || isLoginView ? (
                        <>
                            <div className="input-field-wrapper" style={{ position: 'relative', marginBottom: '1rem' }}>
                                <Mail className="search-icon" size={20} style={{ top: '50%', transform: 'translateY(-50%)' }} />
                                <input
                                    className="input-field search-input"
                                    type="email"
                                    placeholder="Your email address"
                                    value={email}
                                    required
                                    onChange={(e) => setEmail(e.target.value)}
                                />
                            </div>
                            {isLoginView && (
                                <div className="input-field-wrapper" style={{ position: 'relative' }}>
                                    <input
                                        className="input-field"
                                        type="password"
                                        placeholder="Enter password"
                                        value={password}
                                        required
                                        onChange={(e) => setPassword(e.target.value)}
                                    />
                                </div>
                            )}
                        </>
                    ) : (
                        <>
                            <div className="input-field-wrapper" style={{ position: 'relative', marginBottom: '1rem' }}>
                                <input
                                    className="input-field"
                                    type="text"
                                    placeholder="Enter 6-digit OTP (123456)"
                                    value={otp}
                                    required
                                    onChange={(e) => setOtp(e.target.value)}
                                />
                            </div>
                            {!isLoginView && (
                                <div className="input-field-wrapper" style={{ position: 'relative' }}>
                                    <input
                                        className="input-field"
                                        type="password"
                                        placeholder="Create password"
                                        value={password}
                                        required
                                        onChange={(e) => setPassword(e.target.value)}
                                    />
                                </div>
                            )}
                        </>
                    )}

                    <button className="btn-primary" disabled={loading}>
                        {loading ? (
                            <Loader2 className="animate-spin" size={20} style={{ animation: 'spin 1s linear infinite' }} />
                        ) : (
                            <>
                                {isLoginView ? 'Login' : (isOtpSent ? 'Verify OTP' : 'Send OTP')}
                                <ArrowRight size={20} />
                            </>
                        )}
                    </button>
                </form>

                <div style={{ marginTop: '1.5rem', fontSize: '0.95rem', color: 'var(--text-muted)' }}>
                    {isLoginView ? (
                        <p>Don't have an account? <button type="button" onClick={() => { setIsLoginView(false); setMessage(''); setOtp(''); setIsOtpSent(false); }} style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', cursor: 'pointer', fontWeight: '600', fontSize: '0.95rem' }}>Sign Up</button></p>
                    ) : (
                        <>
                            {isOtpSent ? (
                                <p><button type="button" onClick={() => { setIsOtpSent(false); setMessage(''); setOtp(''); }} style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', cursor: 'pointer', fontWeight: '600', fontSize: '0.95rem' }}>Change Email</button></p>
                            ) : (
                                <p>Already have an account? <button type="button" onClick={() => { setIsLoginView(true); setMessage(''); setOtp(''); setIsOtpSent(false); }} style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', cursor: 'pointer', fontWeight: '600', fontSize: '0.95rem' }}>Login</button></p>
                            )}
                        </>
                    )}
                </div>

                {message && (
                    <div style={{ marginTop: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', color: isSuccess ? '#4ade80' : 'var(--text-muted)', fontSize: '0.9rem' }}>
                        {isSuccess && <CheckCircle2 size={16} />}
                        <span style={!isSuccess && message.includes('Invalid') ? { color: '#f87171' } : {}}>{message}</span>
                    </div>
                )}
            </div>
            <style>{`
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
        </div>
    )
}
