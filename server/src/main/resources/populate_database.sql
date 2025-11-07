
BEGIN;

INSERT OR IGNORE INTO users(username,password,is_admin) VALUES('admin','admin',1);

INSERT OR IGNORE INTO movies(title,director,year,rating,rating_amount,synopsis) VALUES('The Matrix','Lana Wachowski',1999,0,0,'A hacker discovers reality is a simulation.');
INSERT OR IGNORE INTO movies(title,director,year,rating,rating_amount,synopsis) VALUES('Inception','Christopher Nolan',2010,0,0,'A thief who steals corporate secrets through dream-sharing technology.');
INSERT OR IGNORE INTO movies(title,director,year,rating,rating_amount,synopsis) VALUES('Spirited Away','Hayao Miyazaki',2001,0,0,'A young girl enters a world of spirits and must save her parents.');
INSERT OR IGNORE INTO movies(title,director,year,rating,rating_amount,synopsis) VALUES('The Godfather','Francis Ford Coppola',1972,0,0,'The aging patriarch of an organized crime dynasty transfers control to his son.');

INSERT OR IGNORE INTO genres(name) VALUES('Ação');
INSERT OR IGNORE INTO genres(name) VALUES('Aventura');
INSERT OR IGNORE INTO genres(name) VALUES('Comédia');
INSERT OR IGNORE INTO genres(name) VALUES('Drama');
INSERT OR IGNORE INTO genres(name) VALUES('Fantasia');
INSERT OR IGNORE INTO genres(name) VALUES('Ficção Científica');
INSERT OR IGNORE INTO genres(name) VALUES('Terror');
INSERT OR IGNORE INTO genres(name) VALUES('Romance');
INSERT OR IGNORE INTO genres(name) VALUES('Documentário');
INSERT OR IGNORE INTO genres(name) VALUES('Musical');
INSERT OR IGNORE INTO genres(name) VALUES('Animação');

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Matrix' LIMIT 1),
    (SELECT id FROM genres WHERE name='Ação' LIMIT 1)
);
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Matrix' LIMIT 1),
    (SELECT id FROM genres WHERE name='Ficção Científica' LIMIT 1)
);

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Inception' LIMIT 1),
    (SELECT id FROM genres WHERE name='Ficção Científica' LIMIT 1)
);
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Inception' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1)
);

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Spirited Away' LIMIT 1),
    (SELECT id FROM genres WHERE name='Animação' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Spirited Away' LIMIT 1),
    (SELECT id FROM genres WHERE name='Fantasia' LIMIT 1)
);

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Godfather' LIMIT 1),
    (SELECT id FROM genres WHERE name='Ação' LIMIT 1)
);
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Godfather' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1)
);

COMMIT;

