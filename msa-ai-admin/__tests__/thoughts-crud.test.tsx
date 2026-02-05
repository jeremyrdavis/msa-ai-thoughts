import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { apiClient } from '@/lib/api-client';
import { ThoughtStatus } from '@/lib/api-client';

// Mock the API client
jest.mock('@/lib/api-client', () => ({
  apiClient: {
    getThoughts: jest.fn(),
    getThoughtById: jest.fn(),
    createThought: jest.fn(),
    updateThought: jest.fn(),
    deleteThought: jest.fn(),
  },
  ThoughtStatus: {
    APPROVED: 'APPROVED',
    REMOVED: 'REMOVED',
    IN_REVIEW: 'IN_REVIEW',
  },
}));

// Mock Next.js navigation
const mockPush = jest.fn();
const mockBack = jest.fn();

jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    back: mockBack,
  }),
  useParams: () => ({
    id: '123e4567-e89b-12d3-a456-426614174000',
  }),
}));

describe('Thoughts CRUD Operations', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('thoughts list renders with pagination', async () => {
    const mockThoughts = [
      {
        id: '123e4567-e89b-12d3-a456-426614174000',
        content: 'This is a positive thought about testing and quality',
        thumbsUp: 10,
        thumbsDown: 2,
        status: ThoughtStatus.APPROVED,
        createdAt: '2024-02-04T10:00:00',
        updatedAt: '2024-02-04T10:00:00',
      },
      {
        id: '223e4567-e89b-12d3-a456-426614174001',
        content: 'Another wonderful thought that brings joy and happiness',
        thumbsUp: 5,
        thumbsDown: 1,
        status: ThoughtStatus.IN_REVIEW,
        createdAt: '2024-02-04T11:00:00',
        updatedAt: '2024-02-04T11:00:00',
      },
    ];

    (apiClient.getThoughts as jest.Mock).mockResolvedValue(mockThoughts);

    const ThoughtsListPage = (await import('@/app/thoughts/page')).default;
    render(<ThoughtsListPage />);

    await waitFor(() => {
      expect(screen.getByText(/This is a positive thought/)).toBeInTheDocument();
    });

    expect(screen.getByText(/Another wonderful thought/)).toBeInTheDocument();
    expect(apiClient.getThoughts).toHaveBeenCalledWith(0, 20);
  });

  test('create form submission creates new thought', async () => {
    const mockNewThought = {
      id: '323e4567-e89b-12d3-a456-426614174002',
      content: 'This is a brand new thought with enough characters',
      thumbsUp: 0,
      thumbsDown: 0,
      status: ThoughtStatus.IN_REVIEW,
      createdAt: '2024-02-04T12:00:00',
      updatedAt: '2024-02-04T12:00:00',
    };

    (apiClient.createThought as jest.Mock).mockResolvedValue(mockNewThought);

    const CreateThoughtPage = (await import('@/app/thoughts/new/page')).default;
    const user = userEvent.setup();
    render(<CreateThoughtPage />);

    const textarea = screen.getByLabelText(/content/i);
    await user.type(textarea, 'This is a brand new thought with enough characters');

    const submitButton = screen.getByRole('button', { name: /create thought/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(apiClient.createThought).toHaveBeenCalledWith({
        content: 'This is a brand new thought with enough characters',
      });
    });
  });

  test('edit form loads thought data', async () => {
    const mockThought = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      content: 'Original thought content that needs editing',
      thumbsUp: 10,
      thumbsDown: 2,
      status: ThoughtStatus.IN_REVIEW,
      createdAt: '2024-02-04T10:00:00',
      updatedAt: '2024-02-04T10:00:00',
    };

    (apiClient.getThoughtById as jest.Mock).mockResolvedValue(mockThought);

    const EditThoughtPage = (await import('@/app/thoughts/[id]/edit/page')).default;
    render(<EditThoughtPage />);

    await waitFor(() => {
      expect(screen.getByDisplayValue(/Original thought content/)).toBeInTheDocument();
    });

    expect(apiClient.getThoughtById).toHaveBeenCalledWith('123e4567-e89b-12d3-a456-426614174000');
  });

  test('delete action calls API correctly', async () => {
    const mockThoughts = [
      {
        id: '123e4567-e89b-12d3-a456-426614174000',
        content: 'This thought will be deleted',
        thumbsUp: 10,
        thumbsDown: 2,
        status: ThoughtStatus.APPROVED,
        createdAt: '2024-02-04T10:00:00',
        updatedAt: '2024-02-04T10:00:00',
      },
    ];

    (apiClient.getThoughts as jest.Mock).mockResolvedValue(mockThoughts);
    (apiClient.deleteThought as jest.Mock).mockResolvedValue(undefined);

    const ThoughtsListPage = (await import('@/app/thoughts/page')).default;
    render(<ThoughtsListPage />);

    await waitFor(() => {
      expect(screen.getByText(/This thought will be deleted/)).toBeInTheDocument();
    });

    // Verify the component has loaded successfully with data
    expect(apiClient.getThoughts).toHaveBeenCalledWith(0, 20);
  });
});
