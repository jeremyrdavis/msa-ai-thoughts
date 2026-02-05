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

describe('Thoughts Author Fields', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('create form includes author and authorBio fields', async () => {
    const CreateThoughtPage = (await import('@/app/thoughts/new/page')).default;
    render(<CreateThoughtPage />);

    expect(screen.getByLabelText('Author')).toBeInTheDocument();
    expect(screen.getByLabelText('Author Bio')).toBeInTheDocument();
  });

  test('edit form displays existing author fields', async () => {
    const mockThought = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      content: 'A great thought about life',
      author: 'Hunter S. Thompson',
      authorBio: 'American journalist and author',
      thumbsUp: 10,
      thumbsDown: 2,
      status: ThoughtStatus.APPROVED,
      createdAt: '2024-02-04T10:00:00',
      updatedAt: '2024-02-04T10:00:00',
    };

    (apiClient.getThoughtById as jest.Mock).mockResolvedValue(mockThought);

    const EditThoughtPage = (await import('@/app/thoughts/[id]/edit/page')).default;
    render(<EditThoughtPage />);

    await waitFor(() => {
      expect(screen.getByDisplayValue('Hunter S. Thompson')).toBeInTheDocument();
    });

    expect(screen.getByDisplayValue('American journalist and author')).toBeInTheDocument();
  });

  test('character counter shows correct count for author field', async () => {
    const CreateThoughtPage = (await import('@/app/thoughts/new/page')).default;
    const user = userEvent.setup();
    render(<CreateThoughtPage />);

    const authorInput = screen.getByPlaceholderText('Enter author name (optional)');
    await user.type(authorInput, 'Hunter S. Thompson');

    await waitFor(() => {
      // "Hunter S. Thompson" is 18 characters
      expect(screen.getByText('18 / 200 characters')).toBeInTheDocument();
    });
  });

  test('character counter shows correct count for authorBio field', async () => {
    const CreateThoughtPage = (await import('@/app/thoughts/new/page')).default;
    const user = userEvent.setup();
    render(<CreateThoughtPage />);

    const authorBioInput = screen.getByPlaceholderText('Enter author bio (optional)');
    await user.type(authorBioInput, 'American journalist');

    await waitFor(() => {
      // "American journalist" is 19 characters
      expect(screen.getByText('19 / 200 characters')).toBeInTheDocument();
    });
  });

  test('form validation prevents submission with more than 200 characters in author', async () => {
    const CreateThoughtPage = (await import('@/app/thoughts/new/page')).default;
    const user = userEvent.setup();
    render(<CreateThoughtPage />);

    const contentInput = screen.getByPlaceholderText('Enter a positive thought...');
    const authorInput = screen.getByPlaceholderText('Enter author name (optional)');
    const longAuthorName = 'A'.repeat(201);

    await user.type(contentInput, 'Valid thought content here');
    await user.type(authorInput, longAuthorName);

    const submitButton = screen.getByRole('button', { name: /create thought/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/must not exceed 200 characters/i)).toBeInTheDocument();
    });

    expect(apiClient.createThought).not.toHaveBeenCalled();
  });

  test('form validation prevents submission with more than 200 characters in authorBio', async () => {
    const CreateThoughtPage = (await import('@/app/thoughts/new/page')).default;
    const user = userEvent.setup();
    render(<CreateThoughtPage />);

    const contentInput = screen.getByPlaceholderText('Enter a positive thought...');
    const authorBioInput = screen.getByPlaceholderText('Enter author bio (optional)');
    const longBio = 'B'.repeat(201);

    await user.type(contentInput, 'Valid thought content here');
    await user.type(authorBioInput, longBio);

    const submitButton = screen.getByRole('button', { name: /create thought/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/must not exceed 200 characters/i)).toBeInTheDocument();
    });

    expect(apiClient.createThought).not.toHaveBeenCalled();
  });

  test('form submits author fields to API correctly', async () => {
    const mockNewThought = {
      id: '323e4567-e89b-12d3-a456-426614174002',
      content: 'This is a brand new thought with author',
      author: 'Hunter S. Thompson',
      authorBio: 'American journalist and author',
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

    const contentInput = screen.getByPlaceholderText('Enter a positive thought...');
    const authorInput = screen.getByPlaceholderText('Enter author name (optional)');
    const authorBioInput = screen.getByPlaceholderText('Enter author bio (optional)');

    await user.type(contentInput, 'This is a brand new thought with author');
    await user.type(authorInput, 'Hunter S. Thompson');
    await user.type(authorBioInput, 'American journalist and author');

    const submitButton = screen.getByRole('button', { name: /create thought/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(apiClient.createThought).toHaveBeenCalledWith({
        content: 'This is a brand new thought with author',
        author: 'Hunter S. Thompson',
        authorBio: 'American journalist and author',
      });
    });
  });

  test('edit form updates author fields via API', async () => {
    const mockThought = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      content: 'Original thought content',
      author: 'Original Author',
      authorBio: 'Original bio',
      thumbsUp: 10,
      thumbsDown: 2,
      status: ThoughtStatus.IN_REVIEW,
      createdAt: '2024-02-04T10:00:00',
      updatedAt: '2024-02-04T10:00:00',
    };

    (apiClient.getThoughtById as jest.Mock).mockResolvedValue(mockThought);
    (apiClient.updateThought as jest.Mock).mockResolvedValue({
      ...mockThought,
      author: 'Updated Author',
      authorBio: 'Updated bio',
    });

    const EditThoughtPage = (await import('@/app/thoughts/[id]/edit/page')).default;
    const user = userEvent.setup();
    render(<EditThoughtPage />);

    await waitFor(() => {
      expect(screen.getByDisplayValue('Original Author')).toBeInTheDocument();
    });

    const authorInput = screen.getByDisplayValue('Original Author');
    const authorBioInput = screen.getByDisplayValue('Original bio');

    await user.clear(authorInput);
    await user.type(authorInput, 'Updated Author');
    await user.clear(authorBioInput);
    await user.type(authorBioInput, 'Updated bio');

    const submitButton = screen.getByRole('button', { name: /update thought/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(apiClient.updateThought).toHaveBeenCalledWith('123e4567-e89b-12d3-a456-426614174000', {
        content: 'Original thought content',
        author: 'Updated Author',
        authorBio: 'Updated bio',
        status: ThoughtStatus.IN_REVIEW,
      });
    });
  });
});
