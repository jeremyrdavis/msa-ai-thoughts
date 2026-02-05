# msa-ai-frontend

User-facing frontend application for viewing random positive thoughts and providing feedback through thumbs up/down ratings.

## Overview

msa-ai-frontend is a Next.js 14+ application built with TypeScript and the App Router. It provides a polished, user-friendly interface for users to:

- View random positive thoughts
- Rate thoughts with thumbs up or thumbs down
- Navigate to view additional random thoughts

This application integrates with the existing Thoughts Service Backend via REST API.

## Tech Stack

- **Framework:** Next.js 14+ with App Router
- **Language:** TypeScript
- **Styling:** Tailwind CSS v4
- **UI Components:** shadcn/ui (Radix UI primitives)
- **Deployment:** Standalone Node.js (configured in next.config.ts)

## Project Structure

```
msa-ai-frontend/
├── app/                    # Next.js App Router pages
│   ├── globals.css        # Global styles and typography
│   ├── layout.tsx         # Root layout
│   └── page.tsx           # Main thought display page
├── components/            # shadcn/ui components
│   └── ui/               # Button, Sonner (toast), Skeleton
├── lib/                   # Library code
│   ├── api-client.ts     # Backend API integration
│   ├── types.ts          # TypeScript interfaces
│   └── utils.ts          # Utility functions
├── public/               # Static assets
├── .env.local            # Environment variables (not committed)
├── .env.local.template   # Environment variable template
├── next.config.ts        # Next.js configuration
└── package.json          # Dependencies
```

## Environment Variables

The application requires the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_BASE_URL` | Base URL for the Thoughts Service Backend API | `http://localhost:8080` |

### Configuration

1. Copy the template file to create your local environment variables:
   ```bash
   cp .env.local.template .env.local
   ```

2. Edit `.env.local` to set the backend API URL:
   ```bash
   NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
   ```

## Local Development Setup

### Prerequisites

- Node.js 18+ and npm
- Running instance of Thoughts Service Backend (default: http://localhost:8080)

### Installation

1. Install dependencies:
   ```bash
   npm install
   ```

2. Configure environment variables (see Environment Variables section above)

3. Start the development server:
   ```bash
   npm run dev
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

The application will hot-reload as you make changes to the code.

## API Integration

The application integrates with the following Thoughts Service Backend endpoints:

### GET /thoughts/random
Fetches a random thought to display to the user.

**Response:**
```json
{
  "id": "uuid",
  "content": "Thought content string",
  "thumbsUp": 0,
  "thumbsDown": 0,
  "status": "APPROVED",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### POST /thoughts/thumbsup/{id}
Records a thumbs up rating for the specified thought.

**Response:** Updated Thought object with incremented thumbsUp count

### POST /thoughts/thumbsdown/{id}
Records a thumbs down rating for the specified thought.

**Response:** Updated Thought object with incremented thumbsDown count

### Error Handling

The API client (`lib/api-client.ts`) includes error handling for:
- Network errors
- 404 Not Found responses
- 400 Bad Request validation errors
- General server errors

Errors are displayed to users via toast notifications using the Sonner component.

## Design Approach

This application follows a polished, user-friendly design philosophy:

- **Typography-focused:** Large, readable text for thought content
- **Responsive design:** Optimized for mobile, tablet, and desktop
- **Engaging interactions:** Smooth hover states and transitions
- **Visual feedback:** Clear indication of user actions (rating selection)
- **Accessibility:** Proper ARIA labels and keyboard navigation

## Deployment Configuration

The application is configured for standalone Node.js deployment:

- **Output mode:** `standalone` (see `next.config.ts`)
- **Build command:** `npm run build`
- **Start command:** `npm start`

After building, Next.js creates a standalone server in `.next/standalone/` that can be deployed to any Node.js environment.

## Scripts

- `npm run dev` - Start development server
- `npm run build` - Build production bundle
- `npm start` - Start production server
- `npm run lint` - Run linter (if configured)

## Component Library

This project uses shadcn/ui components:

- **Button** - Rating buttons and navigation
- **Sonner** - Toast notifications for success/error messages
- **Skeleton** - Loading states during data fetching

To add additional shadcn/ui components:
```bash
npx shadcn@latest add [component-name]
```

## Learn More

- [Next.js Documentation](https://nextjs.org/docs)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [shadcn/ui Documentation](https://ui.shadcn.com)
- [Radix UI Documentation](https://www.radix-ui.com)
