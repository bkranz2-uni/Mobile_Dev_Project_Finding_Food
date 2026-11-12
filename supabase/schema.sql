--
-- PostgreSQL database dump
--

\restrict R8VFrvMVA9jOehSODZFqC1RynhyMMyfClTvEA6526U6OlISyHYXfAcHVAf04bts

-- Dumped from database version 17.11
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: handle_new_user(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.handle_new_user() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO ''
    AS $$
begin
    insert into public.profiles (id)
    values (new.id);
    return new;
end;
$$;


--
-- Name: set_updated_at(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
    new.updated_at = now();
    return new;
end;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: product_stores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_stores (
    store_id bigint NOT NULL,
    report_number text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    added_by uuid DEFAULT auth.uid()
);


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    report_number text NOT NULL,
    product_name_kr text NOT NULL,
    product_name_en text,
    product_kind text,
    raw_materials text,
    allergy_kr text,
    allergy_en text,
    manufacturer text,
    image_url text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    is_vegan boolean DEFAULT false NOT NULL,
    is_vegetarian boolean DEFAULT false NOT NULL,
    is_gluten_free boolean DEFAULT false NOT NULL
);


--
-- Name: profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profiles (
    id uuid NOT NULL,
    display_name text,
    vegan boolean DEFAULT false NOT NULL,
    vegetarian boolean DEFAULT false NOT NULL,
    gluten_free boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: stores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stores (
    id bigint NOT NULL,
    kakao_place_id text NOT NULL,
    name text NOT NULL,
    chain text DEFAULT 'other'::text NOT NULL,
    address text,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT stores_chain_check CHECK ((chain = ANY (ARRAY['CU'::text, 'GS25'::text, '7-Eleven'::text, 'emart24'::text, 'other'::text])))
);


--
-- Name: stores_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.stores ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.stores_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product_stores product_stores_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_stores
    ADD CONSTRAINT product_stores_pkey PRIMARY KEY (store_id, report_number);


--
-- Name: products products_new_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_new_pkey1 PRIMARY KEY (report_number);


--
-- Name: profiles profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profiles
    ADD CONSTRAINT profiles_pkey PRIMARY KEY (id);


--
-- Name: stores stores_kakao_place_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stores
    ADD CONSTRAINT stores_kakao_place_id_key UNIQUE (kakao_place_id);


--
-- Name: stores stores_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stores
    ADD CONSTRAINT stores_pkey PRIMARY KEY (id);


--
-- Name: product_stores_added_by_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX product_stores_added_by_idx ON public.product_stores USING btree (added_by);


--
-- Name: product_stores_product_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX product_stores_product_idx ON public.product_stores USING btree (report_number);


--
-- Name: stores_chain_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX stores_chain_idx ON public.stores USING btree (chain);


--
-- Name: stores_location_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX stores_location_idx ON public.stores USING btree (latitude, longitude);


--
-- Name: profiles profiles_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER profiles_updated_at BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: product_stores product_stores_added_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_stores
    ADD CONSTRAINT product_stores_added_by_fkey FOREIGN KEY (added_by) REFERENCES auth.users(id) ON DELETE SET NULL;


--
-- Name: product_stores product_stores_report_number_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_stores
    ADD CONSTRAINT product_stores_report_number_fkey FOREIGN KEY (report_number) REFERENCES public.products(report_number) ON DELETE CASCADE;


--
-- Name: product_stores product_stores_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_stores
    ADD CONSTRAINT product_stores_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id) ON DELETE CASCADE;


--
-- Name: profiles profiles_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profiles
    ADD CONSTRAINT profiles_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE;


--
-- Name: products Alle kan lese produkter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Alle kan lese produkter" ON public.products FOR SELECT USING (true);


--
-- Name: product_stores Anyone can read product stores; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Anyone can read product stores" ON public.product_stores FOR SELECT TO authenticated, anon USING (true);


--
-- Name: stores Anyone can read stores; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Anyone can read stores" ON public.stores FOR SELECT TO authenticated, anon USING (true);


--
-- Name: products Innloggede kan legge til produkter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Innloggede kan legge til produkter" ON public.products FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: products Innloggede kan oppdatere produkter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Innloggede kan oppdatere produkter" ON public.products FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: product_stores Signed-in users can add products to stores; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Signed-in users can add products to stores" ON public.product_stores FOR INSERT TO authenticated WITH CHECK ((added_by = ( SELECT auth.uid() AS uid)));


--
-- Name: profiles Users can read own profile; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Users can read own profile" ON public.profiles FOR SELECT TO authenticated USING ((id = ( SELECT auth.uid() AS uid)));


--
-- Name: product_stores Users can remove own product links; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Users can remove own product links" ON public.product_stores FOR DELETE TO authenticated USING ((added_by = ( SELECT auth.uid() AS uid)));


--
-- Name: profiles Users can update own profile; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE TO authenticated USING ((id = ( SELECT auth.uid() AS uid))) WITH CHECK ((id = ( SELECT auth.uid() AS uid)));


--
-- Name: product_stores; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.product_stores ENABLE ROW LEVEL SECURITY;

--
-- Name: products; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

--
-- Name: profiles; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

--
-- Name: stores; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.stores ENABLE ROW LEVEL SECURITY;

--
-- PostgreSQL database dump complete
--

\unrestrict R8VFrvMVA9jOehSODZFqC1RynhyMMyfClTvEA6526U6OlISyHYXfAcHVAf04bts

