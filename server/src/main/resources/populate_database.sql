
BEGIN;

-- Users (username is UNIQUE, INSERT OR IGNORE is safe)
INSERT OR IGNORE INTO users(username,password,is_admin) VALUES('admin','$2a$12$DusYaZKU1zsJ0R3yGzIgVOxOmJ4fG0QT1ln4vmzHvePal3HSVwuBS',1);
INSERT OR IGNORE INTO users(username,password,is_admin) VALUES('sapo','$2a$12$KYutxYfQWHocM8HdJNagSuyPZSfRPHT4.W7UbBZ9.Cwk65/CNDcW2',0);
INSERT OR IGNORE INTO users(username,password,is_admin) VALUES('carlos','$2a$12$I/CQNBg/ygROHyIhmykmAOyA1urx7nj1DRgWvu7RuOCsXzQiRtjTW',0);
INSERT OR IGNORE INTO users(username,password,is_admin) VALUES('marina','$2a$12$808OxdcWCC5aX.QZ18GBpuv3fUZ5TvMHk.e3stIwYUs52Uz1d.fBG',0);
INSERT OR IGNORE INTO users(username,password,is_admin) VALUES('lucas','$2a$12$K8nNvSbSEIwLS5BuCceowej6wVkbuu8ysB.FBwqUlQq3L3CZHCE6a',0);

-- Genres (name is UNIQUE, INSERT OR IGNORE is safe)
INSERT OR IGNORE INTO genres(name) VALUES('Action');
INSERT OR IGNORE INTO genres(name) VALUES('Adventure');
INSERT OR IGNORE INTO genres(name) VALUES('Comedy');
INSERT OR IGNORE INTO genres(name) VALUES('Drama');
INSERT OR IGNORE INTO genres(name) VALUES('Fantasy');
INSERT OR IGNORE INTO genres(name) VALUES('Sci-Fi');
INSERT OR IGNORE INTO genres(name) VALUES('Horror');
INSERT OR IGNORE INTO genres(name) VALUES('Romance');
INSERT OR IGNORE INTO genres(name) VALUES('Documentary');
INSERT OR IGNORE INTO genres(name) VALUES('Musical');
INSERT OR IGNORE INTO genres(name) VALUES('Animation');
INSERT OR IGNORE INTO genres(name) VALUES('Crime');
INSERT OR IGNORE INTO genres(name) VALUES('Thriller');
INSERT OR IGNORE INTO genres(name) VALUES('Historical');

-- Movies: no UNIQUE constraint, use WHERE NOT EXISTS to avoid duplicates on re-run
INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'The Matrix','Lana Wachowski',1999,0,0,'A hacker discovers reality is a simulation.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='The Matrix' AND director='Lana Wachowski');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Inception','Christopher Nolan',2010,0,0,'A thief who steals corporate secrets through dream-sharing technology.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Inception' AND director='Christopher Nolan' AND year=2010);

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Spirited Away','Hayao Miyazaki',2001,0,0,'A young girl enters a world of spirits and must save her parents.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Spirited Away' AND director='Hayao Miyazaki');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'The Godfather','Francis Ford Coppola',1972,0,0,'The aging patriarch of an organized crime dynasty transfers control to his son.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='The Godfather' AND director='Francis Ford Coppola');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'The Shawshank Redemption','Frank Darabont',1994,0,0,'Two imprisoned men bond over years, finding solace and eventual redemption.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='The Shawshank Redemption' AND director='Frank Darabont');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Pulp Fiction','Quentin Tarantino',1994,0,0,'Interconnected stories of crime and redemption set in Los Angeles.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Pulp Fiction' AND director='Quentin Tarantino');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'The Dark Knight','Christopher Nolan',2008,0,0,'Batman faces the Joker, a criminal mastermind who plunges Gotham into chaos.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='The Dark Knight' AND director='Christopher Nolan');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Forrest Gump','Robert Zemeckis',1994,0,0,'A kind-hearted man with a low IQ witnesses key events in American history.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Forrest Gump' AND director='Robert Zemeckis');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Fight Club','David Fincher',1999,0,0,'An insomniac and a soap salesman start an underground fight club with dark consequences.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Fight Club' AND director='David Fincher');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT '2001: A Space Odyssey','Stanley Kubrick',1968,0,0,'A voyage to Jupiter turns sinister when the ship''s AI develops its own agenda.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='2001: A Space Odyssey' AND director='Stanley Kubrick');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Parasite','Bong Joon-ho',2019,0,0,'A poor family infiltrates a wealthy household with increasingly dark results.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Parasite' AND director='Bong Joon-ho');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Schindler''s List','Steven Spielberg',1993,0,0,'A German businessman saves over a thousand Jewish lives during the Holocaust.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Schindler''s List' AND director='Steven Spielberg');

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Interstellar','Christopher Nolan',2014,0,0,'Astronauts travel through a wormhole near Saturn searching for a new home for humanity.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Interstellar' AND director='Christopher Nolan' AND year=2014);

INSERT INTO movies(title,director,year,rating,rating_amount,synopsis)
SELECT 'Goodfellas','Martin Scorsese',1990,0,0,'The rise and fall of a mob associate spanning three decades of organised crime.'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title='Goodfellas' AND director='Martin Scorsese');

-- Movie genres (movie_genres has PRIMARY KEY (movie_id, genre_id), INSERT OR IGNORE is safe)
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Matrix' LIMIT 1),
    (SELECT id FROM genres WHERE name='Action' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Matrix' LIMIT 1),
    (SELECT id FROM genres WHERE name='Sci-Fi' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Inception' AND year=2010 LIMIT 1),
    (SELECT id FROM genres WHERE name='Sci-Fi' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Inception' AND year=2010 LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Spirited Away' LIMIT 1),
    (SELECT id FROM genres WHERE name='Animation' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Spirited Away' LIMIT 1),
    (SELECT id FROM genres WHERE name='Fantasy' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Godfather' LIMIT 1),
    (SELECT id FROM genres WHERE name='Crime' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Godfather' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Shawshank Redemption' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Pulp Fiction' LIMIT 1),
    (SELECT id FROM genres WHERE name='Crime' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Pulp Fiction' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1),
    (SELECT id FROM genres WHERE name='Action' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1),
    (SELECT id FROM genres WHERE name='Crime' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Forrest Gump' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Forrest Gump' LIMIT 1),
    (SELECT id FROM genres WHERE name='Romance' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Fight Club' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Fight Club' LIMIT 1),
    (SELECT id FROM genres WHERE name='Thriller' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='2001: A Space Odyssey' LIMIT 1),
    (SELECT id FROM genres WHERE name='Sci-Fi' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='2001: A Space Odyssey' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Parasite' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Parasite' LIMIT 1),
    (SELECT id FROM genres WHERE name='Thriller' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Schindler''s List' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Schindler''s List' LIMIT 1),
    (SELECT id FROM genres WHERE name='Historical' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1),
    (SELECT id FROM genres WHERE name='Sci-Fi' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1),
    (SELECT id FROM genres WHERE name='Adventure' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Goodfellas' LIMIT 1),
    (SELECT id FROM genres WHERE name='Crime' LIMIT 1));
INSERT OR IGNORE INTO movie_genres(movie_id,genre_id) VALUES(
    (SELECT id FROM movies WHERE title='Goodfellas' LIMIT 1),
    (SELECT id FROM genres WHERE name='Drama' LIMIT 1));

-- Reviews: use WHERE NOT EXISTS to avoid duplicates on re-run
INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='The Matrix' LIMIT 1),
    (SELECT id FROM users WHERE username='carlos'),
    5,'Revolucionario','Redefiniu o cinema de ficcao cientifica. Os efeitos visuais e a filosofia por tras sao unicos.',0,'2024-09-12'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='The Matrix' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='carlos'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='The Matrix' LIMIT 1),
    (SELECT id FROM users WHERE username='marina'),
    4,'Muito bom mas denso','Excelente filme, mas a primeira vez pode ser confusa. Na segunda assistida fica perfeito.',0,'2024-10-03'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='The Matrix' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='marina'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='The Godfather' LIMIT 1),
    (SELECT id FROM users WHERE username='lucas'),
    5,'Obra prima absoluta','O melhor filme ja feito. Atuacoes perfeitas, roteiro impecavel e trilha sonora inesquecivel.',0,'2024-08-20'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='The Godfather' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='lucas'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='The Godfather' LIMIT 1),
    (SELECT id FROM users WHERE username='carlos'),
    5,'Imprescindivel','Todo amante de cinema precisa assistir. Uma das maiores obras da historia do cinema.',0,'2024-11-05'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='The Godfather' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='carlos'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='Pulp Fiction' LIMIT 1),
    (SELECT id FROM users WHERE username='marina'),
    5,'Tarantino no apice','Narrativa nao linear genial. Cada cena e memoravel, do inicio ao fim.',0,'2024-07-18'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='Pulp Fiction' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='marina'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1),
    (SELECT id FROM users WHERE username='lucas'),
    5,'Coringa perfeito','Heath Ledger como Coringa e a melhor atuacao de vilao ja vista. Filme impecavel.',0,'2024-06-30'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='lucas'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1),
    (SELECT id FROM users WHERE username='sapo'),
    4,'Quase perfeito','Um dos melhores filmes de super-heroi de todos os tempos. O Coringa rouba cada cena.',0,'2024-12-01'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='The Dark Knight' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='sapo'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='Forrest Gump' LIMIT 1),
    (SELECT id FROM users WHERE username='carlos'),
    5,'Emocionante demais','Uma jornada linda atraves da historia americana. Impossivel nao se emocionar.',0,'2025-01-14'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='Forrest Gump' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='carlos'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='Parasite' LIMIT 1),
    (SELECT id FROM users WHERE username='marina'),
    5,'Cinema coreano no topo','Bong Joon-ho entregou uma obra prima. A tensao aumenta a cada minuto.',0,'2025-02-22'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='Parasite' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='marina'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='Parasite' LIMIT 1),
    (SELECT id FROM users WHERE username='lucas'),
    4,'Surpreendente','Nao esperava tanto. A virada da trama e magistral e deixa o espectador de queixo caido.',0,'2025-03-10'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='Parasite' LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='lucas'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1),
    (SELECT id FROM users WHERE username='sapo'),
    5,'Espetacular','Nolan no seu melhor. A escala visual e emocional do filme e inigualavel.',0,'2025-04-05'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='sapo'));

INSERT INTO reviews(movie_id,user_id,rating,title,description,edited,date)
SELECT
    (SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1),
    (SELECT id FROM users WHERE username='carlos'),
    4,'Muito ambicioso','Impressionante tecnicamente, mas o roteiro fica confuso no terceiro ato. Vale demais.',0,'2025-05-17'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE movie_id=(SELECT id FROM movies WHERE title='Interstellar' AND year=2014 LIMIT 1) AND user_id=(SELECT id FROM users WHERE username='carlos'));

COMMIT;
