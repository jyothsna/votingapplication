

DROP TABLE IF EXISTS movie;
CREATE TABLE movie (
  id serial PRIMARY KEY,
	
  title varchar(255) NOT NULL,
  genre varchar(255) NOT NULL,
   votes INTEGER NOT NULL
   
   
);


DROP TABLE IF EXISTS judge;
CREATE TABLE judge (
  id serial PRIMARY KEY,
	
  name varchar(255) NOT NULL,
  voted boolean NOT NULL
   
   
);



