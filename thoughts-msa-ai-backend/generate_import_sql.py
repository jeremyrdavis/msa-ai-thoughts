#!/usr/bin/env python3
import json
import sys

# Read the JSON file
with open('/Users/jeremyrdavis/Workspace/DevHub/quotes_transformed.json', 'r') as f:
    quotes = json.load(f)

# Generate SQL INSERT statements
print("-- Import quotes from quotes_transformed.json")
print("-- Generated SQL INSERT statements")
print()

for quote in quotes:
    # Escape single quotes in the quote text, author, and bio
    content = quote['quote'].replace("'", "''")
    author = quote['author'].replace("'", "''")
    author_bio = quote['author_bio'].replace("'", "''") if quote['author_bio'] else 'Unknown'
    status = quote['status']

    # Generate INSERT statement
    sql = f"""INSERT INTO thought (content, author, authorBio, status, thumbsUp, thumbsDown, createdAt, updatedAt)
VALUES ('{content}', '{author}', '{author_bio}', '{status}', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);"""

    print(sql)
    print()
