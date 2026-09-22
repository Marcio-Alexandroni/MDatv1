INSERT INTO musicas (titulo, artista, album, duracao, genero) VALUES
('Imagine', 'John Lennon', 'Imagine', 183, 'Rock'),
('Billie Jean', 'Michael Jackson', 'Thriller', 294, 'Pop'),
('Bohemian Rhapsody', 'Queen', 'A Night at the Opera', 354, 'Rock'),
('Garota de Ipanema', 'Tom Jobim', 'Getz/Gilberto', 328, 'Bossa Nova'),
('Tempo Perdido', 'Legião Urbana', 'Dois', 301, 'Rock');

INSERT INTO playlists (nome, descricao) VALUES
('Clássicos do Rock', 'Grandes clássicos do rock internacional'),
('Música Brasileira', 'Clássicos da música brasileira'),
('Pop Internacional', 'Sucessos do pop internacional'),
('Para Relaxar', 'Músicas para momentos tranquilos'),
('Favoritas', 'Minha seleção pessoal de músicas favoritas');

INSERT INTO playlist_musicas (playlistid, musicaid) VALUES
(1, 1), (1, 3), (1, 5), (2, 4), (3, 2), (4, 1), (4, 4), (5, 1), (5, 2), (5, 5);

INSERT INTO reproducoes (playlistid, datahora) VALUES
(1, CURRENT_TIMESTAMP), (1, CURRENT_TIMESTAMP), (1, CURRENT_TIMESTAMP),
(1, CURRENT_TIMESTAMP), (1, CURRENT_TIMESTAMP),
(2, CURRENT_TIMESTAMP), (2, CURRENT_TIMESTAMP), (2, CURRENT_TIMESTAMP),
(3, CURRENT_TIMESTAMP), (3, CURRENT_TIMESTAMP),
(4, CURRENT_TIMESTAMP), (5, CURRENT_TIMESTAMP);
