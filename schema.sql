CREATE TABLE stop_place (
    stop_id  TEXT PRIMARY KEY,          -- f.eks. 'NSR:StopPlace:12345'
    name     TEXT NOT NULL
);

CREATE TABLE quay (
    quay_id      TEXT PRIMARY KEY,      -- f.eks. 'NSR:Quay:67890'
    stop_id      TEXT NOT NULL REFERENCES stop_place(stop_id),
    public_code  TEXT,                  -- f.eks. 'A' eller 'B' / '1' eller '2'
    description  TEXT                   -- f.eks. 'mot sentrum'
);

CREATE TABLE departure (
    id                  BIGSERIAL PRIMARY KEY,
    service_journey_id  TEXT        NOT NULL,
    service_date        DATE        NOT NULL,
    quay_id             TEXT        NOT NULL REFERENCES quay(quay_id),
    line_code           TEXT,
    destination         TEXT,
    aimed_time          TIMESTAMPTZ NOT NULL,
    expected_time       TIMESTAMPTZ,
    realtime            BOOLEAN     NOT NULL DEFAULT FALSE,
    cancelled           BOOLEAN     NOT NULL DEFAULT FALSE,
    first_seen          TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (service_journey_id, service_date, quay_id)
);

CREATE TABLE observation (
    id             BIGSERIAL PRIMARY KEY,
    departure_id   BIGINT      NOT NULL REFERENCES departure(id) ON DELETE CASCADE,
    observed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    expected_time  TIMESTAMPTZ,
    realtime       BOOLEAN     NOT NULL
);

CREATE INDEX idx_departure_aimed ON departure (quay_id, aimed_time);
CREATE INDEX idx_observation_dep ON observation (departure_id, observed_at);



-- Start busstoppene og informasjon om stoppene. 

INSERT INTO stop_place VALUES ('NSR:StopPlace:31821', 'Gyldenpris Nord');
INSERT INTO stop_place VALUES ('NSR:StopPlace:31806', 'Gyldenpris');


INSERT INTO quay VALUES('NSR:Quay:54833', 'NSR:StopPlace:31821', '1', 'mot Laksevåg');
INSERT INTO quay VALUES('NSR:Quay:54834', 'NSR:StopPlace:31821', '2', 'mot sentrum');
INSERT INTO quay VALUES('NSR:Quay:54806', 'NSR:StopPlace:31806', '1', 'mot Fyllingsdalen');
INSERT INTO quay VALUES('NSR:Quay:54807', 'NSR:StopPlace:31806', '2', 'mot sentrum');

