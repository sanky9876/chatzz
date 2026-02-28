import { useState, useEffect } from 'react'
import { supabase } from './supabaseClient'
import Auth from './components/Auth'
import Dashboard from './components/Dashboard'

function App() {
  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session)
      setLoading(false)
    })

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session)
    })

    return () => subscription.unsubscribe()
  }, [])

  if (loading) {
    return (
      <div className="auth-container">
        <div className="auth-card glass-panel" style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <div className="auth-logo" style={{ animation: 'pulseGlow 2s infinite' }}></div>
        </div>
      </div>
    )
  }

  return (
    <>
      {!session ? <Auth /> : <Dashboard session={session} />}
    </>
  )
}

export default App
