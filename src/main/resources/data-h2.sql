insert into categories (id, name) values(1, 'Cats');
insert into categories (id, name) values(2, 'Dogs');

insert into pets (id, name, status, category_id) values(1, 'Fluffy', 0, 1);
insert into pets (id, name, status, category_id) values(2, 'Spot', 0, 2);
insert into pets (id, name, status, category_id) values(3, 'Cthulu', 0, 2);

insert into media (id, name, url) values(1, 'picture 1', 'http://imgur.com/1');
insert into media (id, name, url) values(2, 'picture 2', 'http://imgur.com/2');

insert into tags (id, name) values(1, 'good with other animals');
insert into tags (id, name) values(2, 'good with kids');

insert into pets_media (pet_entity_id, media_id) values(1, 1);
insert into pets_media (pet_entity_id, media_id) values(2, 2);

insert into pet_tags (pet_id, tag_id) values(1, 1);
insert into pet_tags (pet_id, tag_id) values(2, 2);