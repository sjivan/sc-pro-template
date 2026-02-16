CREATE TABLE country (
    country_code VARCHAR(10) NOT NULL PRIMARY KEY,
    country_name VARCHAR(100),
    continent VARCHAR(50),
    area BIGINT,
    population BIGINT,
    gdp BIGINT,
    independence DATE,
    government VARCHAR(200),
    government_desc INTEGER,
    capital VARCHAR(100),
    member_g8 BOOLEAN,
    article VARCHAR(500)
);
