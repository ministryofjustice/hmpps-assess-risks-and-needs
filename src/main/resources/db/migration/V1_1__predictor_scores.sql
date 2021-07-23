CREATE TABLE IF NOT EXISTS offender_predictor
(
    offender_predictor_id           serial          primary key,
    offender_predictor_uuid         uuid            not null unique,
    predictor_type                  varchar(100)    not null,
    algorithm_version               varchar(10)     not null,
    predictor_score                 varchar(10)     not null,
    crn                             varchar(20)     not null,
    predictor_trigger_source        varchar(200)    not null,
    source_id                       varchar(200)    not null,
    source_answers                  JSONB,
    created_by                      text            not null,
    created_date                    timestamp       not null,
    CONSTRAINT unique_source UNIQUE(risk_source, source_id)
);

CREATE INDEX idx_crn ON offender_predictor(crn);
CREATE INDEX idx_uuid ON offender_predictor(offender_predictor_uuid);