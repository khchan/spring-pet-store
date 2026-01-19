-- Categories
insert into categories (id, name) values(1, 'Cats');
insert into categories (id, name) values(2, 'Dogs');

-- Breeds
insert into breeds (id, name, description, size) values(1, 'Persian', 'Long-haired, gentle cats', 'MEDIUM');
insert into breeds (id, name, description, size) values(2, 'Siamese', 'Vocal and social cats', 'MEDIUM');
insert into breeds (id, name, description, size) values(3, 'Golden Retriever', 'Friendly and intelligent dogs', 'LARGE');
insert into breeds (id, name, description, size) values(4, 'Labrador', 'Energetic and loyal dogs', 'LARGE');
insert into breeds (id, name, description, size) values(5, 'Beagle', 'Small hunting dogs, great with families', 'MEDIUM');

-- Owners (with embedded addresses)
insert into owners (id, first_name, last_name, email, phone, street, city, state, zip_code, country)
values(1, 'Alice', 'Johnson', 'alice.johnson@example.com', '555-0101', '123 Maple St', 'Boston', 'MA', '02101', 'USA');

insert into owners (id, first_name, last_name, email, phone, street, city, state, zip_code, country)
values(2, 'Bob', 'Smith', 'bob.smith@example.com', '555-0102', '456 Oak Ave', 'Cambridge', 'MA', '02139', 'USA');

insert into owners (id, first_name, last_name, email, phone, street, city, state, zip_code, country)
values(3, 'Carol', 'Davis', 'carol.davis@example.com', '555-0103', '789 Pine Rd', 'Somerville', 'MA', '02143', 'USA');

-- Clinics (with embedded addresses)
insert into clinics (id, name, phone, street, city, state, zip_code, country)
values(1, 'Boston Veterinary Clinic', '555-1000', '100 Medical Dr', 'Boston', 'MA', '02115', 'USA');

insert into clinics (id, name, phone, street, city, state, zip_code, country)
values(2, 'Cambridge Animal Hospital', '555-2000', '200 Care Ln', 'Cambridge', 'MA', '02138', 'USA');

-- Veterinarians
insert into veterinarians (id, first_name, last_name, specialty, license_number, clinic_id)
values(1, 'Emily', 'Brown', 'General Practice', 'VET-2024-001', 1);

insert into veterinarians (id, first_name, last_name, specialty, license_number, clinic_id)
values(2, 'Michael', 'Chen', 'Surgery', 'VET-2024-002', 1);

insert into veterinarians (id, first_name, last_name, specialty, license_number, clinic_id)
values(3, 'Sarah', 'Williams', 'Dentistry', 'VET-2024-003', 2);

-- Pet Insurance (need to insert before pets that reference them)
insert into pet_insurance (id, policy_number, provider, coverage_amount, monthly_premium, start_date, end_date, status)
values(1, 'POL-2024-001', 'PetHealth Insurance Co', 5000.00, 49.99, '2024-01-01', '2025-01-01', 'ACTIVE');

insert into pet_insurance (id, policy_number, provider, coverage_amount, monthly_premium, start_date, end_date, status)
values(2, 'POL-2024-002', 'SafePet Insurance', 10000.00, 79.99, '2024-03-01', '2025-03-01', 'ACTIVE');

insert into pet_insurance (id, policy_number, provider, coverage_amount, monthly_premium, start_date, end_date, status)
values(3, 'POL-2024-003', 'BestFriend Coverage', 3000.00, 29.99, '2023-12-01', '2024-12-01', 'ACTIVE');

-- Pets (updated with owner_id, breed_id, and insurance_id)
insert into pets (id, name, status, category_id, owner_id, breed_id, insurance_id)
values(1, 'Fluffy', 'AVAILABLE', 1, 1, 1, 1);

insert into pets (id, name, status, category_id, owner_id, breed_id, insurance_id)
values(2, 'Spot', 'AVAILABLE', 2, 1, 3, null);

insert into pets (id, name, status, category_id, owner_id, breed_id, insurance_id)
values(3, 'Cthulu', 'AVAILABLE', 2, 2, 4, 2);

insert into pets (id, name, status, category_id, owner_id, breed_id, insurance_id)
values(4, 'Whiskers', 'AVAILABLE', 1, 2, 2, null);

insert into pets (id, name, status, category_id, owner_id, breed_id, insurance_id)
values(5, 'Buddy', 'PENDING', 2, 3, 5, 3);

-- Media
insert into media (id, name, url) values(1, 'picture 1', 'http://imgur.com/1');
insert into media (id, name, url) values(2, 'picture 2', 'http://imgur.com/2');

-- Tags
insert into tags (id, name) values(1, 'good with other animals');
insert into tags (id, name) values(2, 'good with kids');

-- Join tables
insert into pets_media (pet_entity_id, media_id) values(1, 1);
insert into pets_media (pet_entity_id, media_id) values(2, 2);

insert into pet_tags (pet_id, tag_id) values(1, 1);
insert into pet_tags (pet_id, tag_id) values(2, 2);
insert into pet_tags (pet_id, tag_id) values(3, 1);
insert into pet_tags (pet_id, tag_id) values(3, 2);

-- Medical Records
insert into medical_records (id, pet_id, veterinarian_id, visit_date, diagnosis, treatment, notes, weight)
values(1, 1, 1, '2024-01-15', 'Annual checkup', 'Vaccinations updated, healthy', 'Cat is in excellent condition', 4.5);

insert into medical_records (id, pet_id, veterinarian_id, visit_date, diagnosis, treatment, notes, weight)
values(2, 2, 1, '2024-02-10', 'Ear infection', 'Antibiotics prescribed', 'Follow up in 2 weeks', 28.3);

insert into medical_records (id, pet_id, veterinarian_id, visit_date, diagnosis, treatment, notes, weight)
values(3, 3, 2, '2024-03-05', 'Dental cleaning', 'Professional cleaning performed', 'Teeth in good condition now', 32.1);

insert into medical_records (id, pet_id, veterinarian_id, visit_date, diagnosis, treatment, notes, weight)
values(4, 5, 3, '2024-04-01', 'Injury assessment', 'Minor sprain, rest recommended', 'Should heal in 1-2 weeks', 12.8);

-- Vaccinations
insert into vaccinations (id, pet_id, vaccine_name, date_administered, next_due_date, administered_by, notes)
values(1, 1, 'Rabies', '2024-01-15', '2025-01-15', 1, 'Annual rabies vaccination');

insert into vaccinations (id, pet_id, vaccine_name, date_administered, next_due_date, administered_by, notes)
values(2, 1, 'FVRCP', '2024-01-15', '2025-01-15', 1, 'Feline viral rhinotracheitis, calicivirus, and panleukopenia');

insert into vaccinations (id, pet_id, vaccine_name, date_administered, next_due_date, administered_by, notes)
values(3, 2, 'Rabies', '2024-02-10', '2025-02-10', 1, 'Annual rabies vaccination');

insert into vaccinations (id, pet_id, vaccine_name, date_administered, next_due_date, administered_by, notes)
values(4, 2, 'DHPP', '2024-02-10', '2025-02-10', 1, 'Distemper, Hepatitis, Parvovirus, Parainfluenza');

insert into vaccinations (id, pet_id, vaccine_name, date_administered, next_due_date, administered_by, notes)
values(5, 3, 'Rabies', '2024-03-05', '2025-03-05', 2, 'Annual rabies vaccination');

insert into vaccinations (id, pet_id, vaccine_name, date_administered, next_due_date, administered_by, notes)
values(6, 5, 'Bordetella', '2024-04-01', '2024-10-01', 3, 'Kennel cough prevention');

-- Appointments
insert into appointments (id, pet_id, veterinarian_id, date_time, reason, notes, status)
values(1, 1, 1, '2024-07-15 10:00:00', 'Annual checkup', 'Regular annual examination', 'SCHEDULED');

insert into appointments (id, pet_id, veterinarian_id, date_time, reason, notes, status)
values(2, 2, 1, '2024-07-20 14:30:00', 'Follow-up for ear infection', 'Check healing progress', 'SCHEDULED');

insert into appointments (id, pet_id, veterinarian_id, date_time, reason, notes, status)
values(3, 3, 2, '2024-08-01 09:00:00', 'Dental checkup', '6-month dental follow-up', 'SCHEDULED');

insert into appointments (id, pet_id, veterinarian_id, date_time, reason, notes, status)
values(4, 4, 3, '2024-07-25 11:00:00', 'New patient exam', 'First visit for this cat', 'CONFIRMED');

insert into appointments (id, pet_id, veterinarian_id, date_time, reason, notes, status)
values(5, 5, 3, '2024-07-18 15:00:00', 'Sprain follow-up', 'Check recovery from injury', 'CONFIRMED');

-- Reset identity sequences to avoid conflicts with manually-inserted IDs
ALTER TABLE categories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE breeds ALTER COLUMN id RESTART WITH 100;
ALTER TABLE owners ALTER COLUMN id RESTART WITH 100;
ALTER TABLE clinics ALTER COLUMN id RESTART WITH 100;
ALTER TABLE veterinarians ALTER COLUMN id RESTART WITH 100;
ALTER TABLE pets ALTER COLUMN id RESTART WITH 100;
ALTER TABLE pet_insurance ALTER COLUMN id RESTART WITH 100;
ALTER TABLE medical_records ALTER COLUMN id RESTART WITH 100;
ALTER TABLE vaccinations ALTER COLUMN id RESTART WITH 100;
ALTER TABLE appointments ALTER COLUMN id RESTART WITH 100;
ALTER TABLE media ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tags ALTER COLUMN id RESTART WITH 100;