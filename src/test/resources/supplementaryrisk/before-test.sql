-- noinspection SqlResolveForFile

insert into supplementary_risk (supplementary_risk_id,
                                supplementary_risk_uuid,
                                risk_source,
                                source_id,
                                crn,
                                risk_comments,
                                created_by,
                                created_by_user_type,
                                created_date)
values
(1, '2e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '182987872', 'X123458', 'risk for children','Gary cooper', 'delius', '2019-11-14 09:00') ,
(2, '4e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '3e020e78-a81c-407f-bc78-e5f284e237e5', 'X123457', 'risk to self', 'Gary C', 'delius', '2019-11-14 09:05'),
(3, '5e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '7e020e78-a81c-407f-bc78-e5f284e237e5', 'X123456', 'risk to self', 'Gary C', 'delius', '2019-11-14 09:07'),
(4, '6e020e78-a81c-407f-bc78-e5f284e237e5', 'INTERVENTION_REFERRAL', '7e020e78-a81c-407f-bc78-e5f284e237e9', 'X123456', 'risk to self', 'Gary C', 'delius', '2019-11-14 09:06');