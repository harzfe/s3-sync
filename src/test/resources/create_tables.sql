CREATE TABLE IF NOT EXISTS auftraege (
    auftragid varchar(255) NOT NULL,
    artikelnummer varchar(255) NOT NULL,
    created varchar(255) NOT NULL,
    lastchange varchar(255) NOT NULL,
    kundeid varchar(255) NOT NULL,
    CONSTRAINT auftraege_pkey PRIMARY KEY (auftragid)
);
CREATE TABLE IF NOT EXISTS kunde (
    kundenid bigserial NOT NULL,
    vorname varchar(100) NOT NULL,
    nachname varchar(100) NOT NULL,
    email varchar(100) NOT NULL,
    strasse varchar(255) NOT NULL,
    strassenzusatz varchar(255) NOT NULL,
    ort varchar(255) NOT NULL,
    land varchar(255) NOT NULL,
    plz varchar(255) NOT NULL,
    firmenname varchar(100) NOT NULL,
    CONSTRAINT kunde_pkey PRIMARY KEY (kundenid)
);
CREATE TABLE IF NOT EXISTS synced_kunde_hash (
    kundenid BIGINT PRIMARY KEY,
    row_hash varchar(255) NOT NULL,
    last_synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS synced_auftrag_hash (
    auftragid varchar(255) PRIMARY KEY,
    marker_hash varchar(255) NOT NULL,
    last_synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);