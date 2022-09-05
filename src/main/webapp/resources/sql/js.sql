---------------------------------------------------------------------------------------------------------
---- 카테고리+헬다

CREATE OR REPLACE VIEW MYPAGEBABYINFO
AS
SELECT  HD_CODE,
        HD_DATE,
        HD_CACODE,
        HD_BBSUCODE,
        HD_BBCODE,
        HD_VALUE,
        CA_NAME,
        CA_TYPE
FROM HD INNER JOIN CA ON CA.CA_CODE = HD.HD_CACODE;

---- 최신 키
CREATE OR REPLACE VIEW BABYRECENTHEIGHT
AS
select HD_BBSUCODE AS HSUCODE,HD_BBCODE AS HBABYCODE ,CA_NAME, HD_VALUE AS BBHEIGHT
                    FROM MYPAGEBABYINFO  
                    WHERE (HD_BBCODE,HD_CACODE, HD_DATE) 
                    IN (
                        SELECT HD_BBCODE, HD_CACODE, MAX(HD_DATE) AS HD_DATE 
                            FROM HEALTHDIARY 
                            GROUP BY HD_BBCODE,HD_CACODE
                       )AND CA_NAME = 'HEIGHT'
                    ORDER BY HD_BBCODE,HD_CACODE;

---- 최신 몸무게
CREATE OR REPLACE VIEW BABYRECENTWEIGHT
AS
select HD_BBSUCODE AS WSUCODE,HD_BBCODE AS WBABYCODE ,CA_NAME, HD_VALUE AS BBWEIGHT
                    FROM MYPAGEBABYINFO  
                    WHERE (HD_BBCODE,HD_CACODE, HD_DATE) 
                    IN (
                        SELECT HD_BBCODE, HD_CACODE, MAX(HD_DATE) AS HD_DATE
                            FROM HEALTHDIARY
                            GROUP BY HD_BBCODE,HD_CACODE
                       )AND CA_NAME = 'WEIGHT'
                    ORDER BY HD_BBCODE,HD_CACODE;

-- 최신키+최신몸무게

SELECT BB_CODE AS BBCODE, BB_SUCODE AS SUCODE, BB_NAME AS BBNAME, TO_CHAR(BB_BIRTHDAY,'YYYY-MM-DD') AS BBBIRTHDAY, BBWEIGHT, BBHEIGHT,BB_PHOTO AS BBPHOTO
FROM BB LEFT OUTER JOIN BABYRECENTWEIGHT ON (BB_CODE = WBABYCODE AND BB_SUCODE = WSUCODE)
        LEFT OUTER JOIN BABYRECENTHEIGHT ON (BB_CODE = HBABYCODE AND BB_SUCODE = HSUCODE)
WHERE BB_SUCODE = '2411751690'
ORDER BY BBCODE;