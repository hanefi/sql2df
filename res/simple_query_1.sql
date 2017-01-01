SELECT 
	id
FROM( 
	SELECT id FROM students WHERE grade > 3.5 ) AS T1;
