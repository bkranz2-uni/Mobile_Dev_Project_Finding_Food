-- Run once in the Supabase SQL Editor. Safe to run again.

-- 1. Products are read-only for app users.
--    Only the team (service_role / SQL Editor, which bypass RLS) may insert or change
--    products, so users can no longer flip is_vegan etc. The app never writes products.
drop policy if exists "Innloggede kan legge til produkter" on public.products;
drop policy if exists "Innloggede kan oppdatere produkter" on public.products;

-- 2. Make sure every new auth user gets a profile row.
--    Only creates the trigger if no trigger on auth.users calls handle_new_user() yet,
--    so an existing trigger (whatever its name) is never duplicated.
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
        raise notice 'Created trigger on_auth_user_created';
    else
        raise notice 'Sign-up trigger already exists, nothing changed';
    end if;
end
$$;

-- 3. Backfill profiles for users who signed up while the trigger was missing.
insert into public.profiles (id)
select u.id
from auth.users u
where not exists (select 1 from public.profiles p where p.id = u.id);

-- Check: should list only "Alle kan lese produkter" for products.
select tablename, policyname, cmd, roles
from pg_policies
where schemaname = 'public'
order by tablename, policyname;
