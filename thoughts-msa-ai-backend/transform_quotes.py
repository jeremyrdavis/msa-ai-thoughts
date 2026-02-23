import json
import re

def transform_quotes(input_file, output_file):
    quotes = []
    with open(input_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            
            # Pattern: line_number:“quote” —author, bio
            # Note: There are different types of dashes: — (em dash), ― (horizontal bar)
            
            # Match the quote inside curly quotes
            quote_match = re.search(r'“([^”]*)”', line)
            if not quote_match:
                continue
            
            quote_text = quote_match.group(1)
            
            # Find the part after the quote
            after_quote = line[quote_match.end():].strip()
            
            # Remove leading dashes and spaces
            after_quote = re.sub(r'^[—―\s]+', '', after_quote)
            
            # Split by comma to get author and bio
            parts = after_quote.split(',', 1)
            author = parts[0].strip()
            author_bio = parts[1].strip() if len(parts) > 1 else ""
            
            # Clean up trailing comma in author_bio if it exists (e.g. line 11)
            author_bio = author_bio.rstrip(',')
            
            quotes.append({
                "quote": quote_text,
                "author": author,
                "author_bio": author_bio
            })
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(quotes, f, indent=3, ensure_ascii=False)

if __name__ == "__main__":
    transform_quotes('/Users/jeremyrdavis/Workspace/DevHub/quotes.json', '/Users/jeremyrdavis/Workspace/DevHub/quotes_transformed.json')
