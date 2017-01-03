SELECT
    count(l_returnflag) AS cnt,
    l_partkey
FROM
    lineitem
ORDER BY
    l_partkey;
