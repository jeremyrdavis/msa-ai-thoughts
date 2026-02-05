import { render, screen, waitFor } from '@testing-library/react';
import Home from '@/app/page';
import { apiClient } from '@/lib/api-client';
import { Thought, ThoughtStatus } from '@/lib/types';

// Mock the API client
jest.mock('@/lib/api-client', () => ({
  apiClient: {
    getRandomThought: jest.fn(),
    thumbsUpThought: jest.fn(),
    thumbsDownThought: jest.fn(),
  },
}));

// Mock toast notifications
jest.mock('sonner', () => ({
  toast: {
    error: jest.fn(),
    success: jest.fn(),
  },
}));

const mockThoughtWithAuthor: Thought = {
  id: '123e4567-e89b-12d3-a456-426614174000',
  content: 'This is a test thought with author',
  thumbsUp: 5,
  thumbsDown: 2,
  status: ThoughtStatus.APPROVED,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
  author: 'Hunter S. Thompson',
  authorBio: 'Author and Journalist',
};

const mockThoughtUnknownAuthor: Thought = {
  id: '987e6543-e89b-12d3-a456-426614174001',
  content: 'This is a thought with unknown author',
  thumbsUp: 3,
  thumbsDown: 1,
  status: ThoughtStatus.APPROVED,
  createdAt: '2024-01-02T00:00:00Z',
  updatedAt: '2024-01-02T00:00:00Z',
  author: 'Unknown',
  authorBio: 'Unknown',
};

describe('Author Attribution Display', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('displays author attribution below thought content', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtWithAuthor);

    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
    });

    // Verify author attribution is displayed
    expect(screen.getByText('Hunter S. Thompson, Author and Journalist')).toBeInTheDocument();
  });

  test('formats author and bio on same line with comma separator', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtWithAuthor);

    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
    });

    // Verify format: "Author Name, Bio Text"
    const authorAttribution = screen.getByText(/Hunter S. Thompson, Author and Journalist/i);
    expect(authorAttribution).toBeInTheDocument();
    expect(authorAttribution.textContent).toBe('Hunter S. Thompson, Author and Journalist');
  });

  test('displays author with smaller font than thought content', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtWithAuthor);

    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
    });

    const thoughtContent = screen.getByText(mockThoughtWithAuthor.content);
    const authorAttribution = screen.getByText('Hunter S. Thompson, Author and Journalist');

    // Thought content should have larger text classes (text-2xl, text-3xl, text-4xl)
    expect(thoughtContent).toHaveClass('text-2xl');

    // Author attribution should have smaller text classes (text-sm or text-base)
    expect(authorAttribution).toHaveClass('text-sm');
  });

  test('displays "Unknown" author correctly', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtUnknownAuthor);

    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtUnknownAuthor.content)).toBeInTheDocument();
    });

    // Verify "Unknown" is displayed for both author and bio
    expect(screen.getByText('Unknown, Unknown')).toBeInTheDocument();
  });

  test('author attribution appears on rating interface', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtWithAuthor);
    (apiClient.thumbsUpThought as jest.Mock).mockResolvedValue({
      ...mockThoughtWithAuthor,
      thumbsUp: 6,
    });

    const { container } = render(<Home />);

    // Wait for thought and rating buttons to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /thumbs up/i })).toBeInTheDocument();
    });

    // Verify author attribution is visible when rating buttons are displayed
    expect(screen.getByText('Hunter S. Thompson, Author and Journalist')).toBeInTheDocument();

    // Verify both author attribution and rating buttons are in the DOM
    const thumbsUpButton = screen.getByRole('button', { name: /thumbs up/i });
    const authorAttribution = screen.getByText('Hunter S. Thompson, Author and Journalist');
    expect(thumbsUpButton).toBeInTheDocument();
    expect(authorAttribution).toBeInTheDocument();
  });

  test('author attribution has center alignment', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtWithAuthor);

    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
    });

    const authorAttribution = screen.getByText('Hunter S. Thompson, Author and Journalist');

    // Verify center alignment styling
    expect(authorAttribution).toHaveClass('text-center');
  });

  test('author attribution has muted color styling', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThoughtWithAuthor);

    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
    });

    const authorAttribution = screen.getByText('Hunter S. Thompson, Author and Journalist');

    // Verify muted color classes (text-zinc-600 dark:text-zinc-400)
    expect(authorAttribution).toHaveClass('text-zinc-600');
  });

  test('loading state includes author attribution skeleton', async () => {
    (apiClient.getRandomThought as jest.Mock).mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve(mockThoughtWithAuthor), 100))
    );

    render(<Home />);

    // Check for loading skeleton
    const skeleton = screen.getByTestId('thought-skeleton');
    expect(skeleton).toBeInTheDocument();

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThoughtWithAuthor.content)).toBeInTheDocument();
    }, { timeout: 200 });
  });
});
