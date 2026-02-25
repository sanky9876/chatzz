-- Create users table (linked to auth.users)
CREATE TABLE public.users (
  id UUID REFERENCES auth.users NOT NULL PRIMARY KEY,
  name TEXT,
  avatar_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  last_seen TIMESTAMPTZ DEFAULT NOW()
);

-- Create chats table
CREATE TABLE public.chats (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  last_message_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create chat_members table (junction)
CREATE TABLE public.chat_members (
  chat_id UUID REFERENCES public.chats(id) ON DELETE CASCADE,
  user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
  PRIMARY KEY (chat_id, user_id)
);

-- Create messages table
CREATE TABLE public.messages (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  chat_id UUID REFERENCES public.chats(id) ON DELETE CASCADE,
  sender_id UUID REFERENCES public.users(id),
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  read_at TIMESTAMPTZ
);

-- Indexes for performance
CREATE INDEX idx_messages_chat_id_created_at ON public.messages(chat_id, created_at DESC);
CREATE INDEX idx_chat_members_user_id ON public.chat_members(user_id);

-- Enable RLS
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chat_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Policies for users
CREATE POLICY "Users can view their own profile and others" ON public.users FOR SELECT USING (true);
CREATE POLICY "Users can update their own profile" ON public.users FOR UPDATE USING (auth.uid() = id);

-- Policies for chats
-- Users can see chats they are members of
CREATE POLICY "Users can view chats they are members of" ON public.chats 
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM public.chat_members 
      WHERE chat_id = public.chats.id AND user_id = auth.uid()
    )
  );

-- Only members can delete chat (simplified for MVP: any member can trigger delete)
CREATE POLICY "Users can delete chats they belong to" ON public.chats
  FOR DELETE USING (
    EXISTS (
      SELECT 1 FROM public.chat_members
      WHERE chat_id = public.chats.id AND user_id = auth.uid()
    )
  );

-- Policies for chat_members
CREATE POLICY "Users can view members of their chats" ON public.chat_members 
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM public.chat_members AS my_membership
      WHERE my_membership.chat_id = public.chat_members.chat_id 
      AND my_membership.user_id = auth.uid()
    )
  );

-- Policies for messages
CREATE POLICY "Users can view messages in their chats" ON public.messages 
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM public.chat_members 
      WHERE chat_id = public.messages.chat_id AND user_id = auth.uid()
    )
  );

CREATE POLICY "Users can insert messages in their chats" ON public.messages 
  FOR INSERT WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.chat_members 
      WHERE chat_id = public.messages.chat_id AND user_id = auth.uid()
    ) AND sender_id = auth.uid()
  );

-- Handle user creation via Trigger from Auth
CREATE OR REPLACE FUNCTION public.handle_new_user() 
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.users (id, name)
  VALUES (new.id, new.raw_user_meta_data->>'name');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- Realtime Setup (Add tables to the 'supabase_realtime' publication)
ALTER PUBLICATION supabase_realtime ADD TABLE public.messages;
ALTER PUBLICATION supabase_realtime ADD TABLE public.chats;
ALTER PUBLICATION supabase_realtime ADD TABLE public.users;
