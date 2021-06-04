CREATE TABLE IF NOT EXISTS supplementary_risk
(
    supplementary_risk_id       serial          primary key,
    supplementary_risk_uuid     uuid            not null unique,
    risk_source                 varchar(200)    not null,
    source_id                   varchar(200)    not null,
    crn                         varchar(20)     not null,
    risk_comments               text            not null,
    created_by                  text            not null,
    created_by_user_type        text            not null,
    created_date                timestamp       not null,
    CONSTRAINT unique_source UNIQUE(risk_source, source_id)
);

CREATE INDEX idx_crn ON supplementary_risk(crn);
CREATE INDEX idx_uuid ON supplementary_risk(supplementary_risk_uuid);