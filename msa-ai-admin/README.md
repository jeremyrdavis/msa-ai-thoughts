# MSA AI Admin

Admin interface for managing thoughts in the MSA AI application.

## Project Overview

This is a Next.js 14+ application built with TypeScript, App Router, and Tailwind CSS. It provides administrative CRUD operations for managing thoughts, including creating, editing, deleting, and viewing thought statistics.

## Tech Stack

- **Framework:** Next.js 15+ with App Router
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **UI Components:** shadcn/ui with Radix UI primitives
- **Form Management:** react-hook-form with Zod validation
- **Icons:** Lucide React
- **Deployment Mode:** Standalone (Node.js)

## Project Structure

```
msa-ai-admin/
├── app/                    # Next.js App Router directory
│   ├── layout.tsx         # Root layout with metadata
│   ├── page.tsx           # Home page
│   └── globals.css        # Global styles with Tailwind
├── components/            # React components
│   ├── ui/               # shadcn/ui components
│   └── client-layout.tsx # Client-side layout wrapper
├── lib/                   # Utility libraries
│   ├── api-client.ts     # Backend API client
│   └── utils.ts          # Utility functions
├── hooks/                 # Custom React hooks
│   └── use-toast.ts      # Toast notification hook
├── public/               # Static assets
├── .env.local.template   # Environment variables template
├── next.config.ts        # Next.js configuration
├── tailwind.config.ts    # Tailwind CSS configuration
├── tsconfig.json         # TypeScript configuration
└── package.json          # Dependencies and scripts
```

## Environment Variables

Create a `.env.local` file in the root directory with the following variables:

```bash
# Backend API Configuration
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

See `.env.local.template` for reference.

## Local Development Setup

### Prerequisites

- Node.js 18+ and npm
- Running instance of the Thoughts Service Backend (default: http://localhost:8080)

### Installation

1. Clone the repository and navigate to the project directory:
   ```bash
   cd msa-ai-admin
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create your environment configuration:
   ```bash
   cp .env.local.template .env.local
   ```

4. Update `.env.local` with your backend API URL if different from default.

### Running the Development Server

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Building for Production

```bash
npm run build
```

This creates an optimized production build in standalone mode.

### Starting Production Server

```bash
npm run start
```

Runs the production build (requires `npm run build` first).

### Linting

```bash
npm run lint
```

## API Integration

The application integrates with the Thoughts Service Backend REST API. All API calls are handled through the centralized `lib/api-client.ts` service.

### API Endpoints Used

- **GET /thoughts?page={page}&size={size}** - Fetch paginated list of thoughts
- **GET /thoughts/{id}** - Fetch single thought by ID
- **POST /thoughts** - Create new thought
- **PUT /thoughts/{id}** - Update thought (content and status)
- **DELETE /thoughts/{id}** - Delete thought

### TypeScript Interfaces

The API client defines TypeScript interfaces that match the backend Thought entity:

```typescript
export interface Thought {
  id: string;
  content: string;
  thumbsUp: number;
  thumbsDown: number;
  status: ThoughtStatus;
  createdAt: string;
  updatedAt: string;
}

export enum ThoughtStatus {
  APPROVED = "APPROVED",
  REMOVED = "REMOVED",
  IN_REVIEW = "IN_REVIEW",
}
```

### Error Handling

The API client includes centralized error handling:

- Network errors are caught and wrapped in `ApiError` objects
- HTTP error responses (4xx, 5xx) include status codes and error messages
- Validation errors from backend are parsed and displayed to users
- Toast notifications are used for user-friendly error messages

## shadcn/ui Components

The following shadcn/ui components are installed and configured:

- **Button** - Primary actions and buttons
- **Table** - Data table display
- **Form** - Form wrapper with react-hook-form integration
- **Input** - Text input fields
- **Textarea** - Multi-line text input
- **Label** - Form field labels
- **Select** - Dropdown selection
- **AlertDialog** - Confirmation dialogs
- **Toast** - Notification messages

All components are located in `components/ui/` and can be imported as:

```typescript
import { Button } from "@/components/ui/button";
import { Table } from "@/components/ui/table";
```

## Deployment Configuration

The application is configured for standalone Node.js deployment:

- **Output Mode:** `standalone` in `next.config.ts`
- **Build Artifact:** `.next/standalone/` directory after build
- **Static Assets:** `.next/static/` must be copied to standalone directory
- **Public Files:** `public/` directory must be copied to standalone directory

### Deployment Steps

1. Build the application:
   ```bash
   npm run build
   ```

2. The standalone server and dependencies are in `.next/standalone/`

3. Copy static assets:
   ```bash
   cp -r .next/static .next/standalone/.next/
   cp -r public .next/standalone/
   ```

4. Run the standalone server:
   ```bash
   cd .next/standalone
   node server.js
   ```

## Design Approach

This admin interface follows a **utilitarian design approach**:

- Clean, functional UI focused on data management
- Minimal decorative elements
- Emphasis on efficiency and clarity
- Data-centric table views
- Clear action buttons and form controls

## Features (To Be Implemented)

The following features will be implemented in subsequent task groups:

- Thoughts list/table view with pagination
- Create new thought form
- Edit thought form (content and status)
- Delete thought with confirmation dialog
- Rating statistics display (thumbs up/down counts and percentages)
- Client-side sorting and filtering
- Loading states and error handling
- Toast notifications for success/error feedback

## License

Copyright (c) Red Hat Demos
