INSERT INTO kunde (
        vorname,
        nachname,
        email,
        strasse,
        strassenzusatz,
        ort,
        land,
        plz,
        firmenname
    )
VALUES (
        'Anna',
        'Müller',
        'anna.mueller@example.com',
        'Hauptstr. 12',
        '',
        'Berlin',
        'Deutschland',
        '10115',
        'Müller Consulting GmbH'
    ),
    (
        'Peter',
        'Schmidt',
        'peter.schmidt@example.com',
        'Marktplatz 5',
        'EG',
        'Hamburg',
        'Deutschland',
        '20095',
        'Schmidt & Partner KG'
    ),
    (
        'Laura',
        'Weber',
        'laura.weber@example.com',
        'Bahnhofstr. 8',
        '',
        'München',
        'Deutschland',
        '80331',
        'Weber Solutions AG'
    ),
    (
        'Michael',
        'Klein',
        'michael.klein@example.com',
        'Goethestr. 23',
        '2. OG',
        'Frankfurt',
        'Deutschland',
        '60313',
        'Klein IT Systems'
    ),
    (
        'Julia',
        'Fischer',
        'julia.fischer@example.com',
        'Seestr. 7',
        '',
        'London',
        'England',
        '70173',
        'Fischer Handels GmbH'
    );
INSERT INTO auftraege (
        auftragid,
        artikelnummer,
        created,
        lastchange,
        kundeid
    )
VALUES (
        'A-1001',
        'ART-001',
        '2025-01-15 10:00:00',
        '2025-01-15 10:00:00',
        '1'
    ),
    (
        'A-1002',
        'ART-002',
        '2025-01-16 09:30:00',
        '2025-01-16 12:45:00',
        '1'
    ),
    (
        'A-1003',
        'ART-003',
        '2025-01-20 14:15:00',
        '2025-01-20 14:20:00',
        '1'
    ),
    (
        'A-1004',
        'ART-004',
        '2025-01-22 08:10:00',
        '2025-01-23 09:00:00',
        '1'
    ),
    (
        'A-1005',
        'ART-005',
        '2025-02-01 11:45:00',
        '2025-02-01 12:00:00',
        '4'
    ),
    (
        'A-1006',
        'ART-006',
        '2025-02-05 16:00:00',
        '2025-02-05 16:30:00',
        '5'
    );
UPDATE kunde
SET land = 'Österreich',
    plz = '5020',
    ort = 'Salzburg'
WHERE kundenid = 5;
UPDATE auftraege
SET artikelnummer = 'ART-001A',
    lastchange = '2025-03-01 10:15:00'
WHERE auftragid = 'A-1001';