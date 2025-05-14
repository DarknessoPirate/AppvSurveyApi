-- Clear tables first (if needed)
DELETE FROM user_answer_selections;
DELETE FROM user_answers;
DELETE FROM submitted_surveys;
DELETE FROM question_answers;
DELETE FROM questions;
DELETE FROM surveys;

-- Insert a survey
INSERT INTO surveys (id, title, description, created_at, expires_at, is_active, access_code)
VALUES (1, 'Customer Satisfaction Survey', 'Help us improve our products and services', CURRENT_TIMESTAMP, '2025-06-15 23:59:59', true, 'abc123');

-- Insert questions with CORRECT enum values
INSERT INTO questions (id, survey_id, text, question_type, required, display_order)
VALUES
    (1, 1, 'How would you rate your overall experience with our product?', 'DROPDOWN', true, 1),
    (2, 1, 'Which features do you use most frequently?', 'CHECKBOX', false, 2),
    (3, 1, 'What improvements would you suggest?', 'OPEN', false, 3);

-- Insert possible answers for question 1
INSERT INTO question_answers (id, question_id, text, display_order)
VALUES
    (1, 1, 'Very Satisfied', 1),
    (2, 1, 'Satisfied', 2),
    (3, 1, 'Neutral', 3),
    (4, 1, 'Dissatisfied', 4),
    (5, 1, 'Very Dissatisfied', 5);

-- Insert possible answers for question 2
INSERT INTO question_answers (id, question_id, text, display_order)
VALUES
    (6, 2, 'Dashboard Analytics', 1),
    (7, 2, 'Report Generation', 2),
    (8, 2, 'Data Visualization', 3),
    (9, 2, 'Collaborative Editing', 4),
    (10, 2, 'Mobile App Integration', 5);

SELECT setval('surveys_id_seq', (SELECT MAX(id) FROM surveys));
SELECT setval('questions_id_seq', (SELECT MAX(id) FROM questions));
SELECT setval('question_answers_id_seq', (SELECT MAX(id) FROM question_answers));