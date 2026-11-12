-- Run in the Supabase SQL Editor. Safe to run again.

-- 1. Products are read-only for users (the team imports via SQL Editor / service_role).
drop policy if exists "Innloggede kan legge til produkter" on public.products;
drop policy if exists "Innloggede kan oppdatere produkter" on public.products;

-- 2. Create the sign-up trigger only if it is missing.
do $$
begin
    if not exists (
        select 1
        from pg_trigger
        where tgrelid = 'auth.users'::regclass
          and tgfoid = 'public.handle_new_user'::regproc
          and not tgisinternal
    ) then
        create trigger on_auth_user_created
            after insert on auth.users
            for each row execute function public.handle_new_user();
    end if;
end
$$;

-- 3. Add profiles for users created while the trigger was missing.
insert into public.profiles (id)
select u.id
from auth.users u
where not exists (select 1 from public.profiles p where p.id = u.id);
