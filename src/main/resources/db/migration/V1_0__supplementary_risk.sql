create table if not exists supplementary_risk
(
    supplementary_risk_id       serial     primary key,
    supplementary_risk_uuid     uuid       not null unique,
    risk_source                 text       not null,
    source_id                   text       not null,
    crn                         text       null,
    risk_comments               text       not null,
    created_by                  text       not null,
    created_by_user_type        text       not null,
    created_date                timestamp  not null
);
