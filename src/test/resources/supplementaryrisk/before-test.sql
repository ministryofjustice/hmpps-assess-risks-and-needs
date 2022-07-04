-- noinspection SqlResolveForFile

insert into supplementary_risk (supplementary_risk_uuid,
                                risk_source,
                                source_id,
                                crn,
                                risk_comments,
                                created_by,
                                created_by_user_type,
                                created_date,
                                risk_answers)
values
('2e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '182987872', 'X123458', 'risk for children','Gary cooper', 'DELIUS', '2019-11-14 09:00', null),
('4e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '3e020e78-a81c-407f-bc78-e5f284e237e5', 'X123457', 'risk to self', 'Gary C', 'DELIUS', '2019-11-14 09:05', null),
('5e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '7e020e78-a81c-407f-bc78-e5f284e237e5', 'X123456', 'risk to self', 'Gary C', 'DELIUS', '2019-11-14 09:07', null),
('6e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '7e020e78-a81c-407f-bc78-e5f284e237e9', 'X123456', 'risk to self', 'Gary C', 'DELIUS', '2019-11-14 09:06',
'{"riskWho":"Risk to person","riskWhen":"When risk is greatest","riskNature":"Nature is risk","concernsSelfHarm":"Self harm concerns","concernsSuicide":"Suicide concerns","concernsHostel":"Hostel concerns","concernsVulnerability":"Vulnerability concerns"}');