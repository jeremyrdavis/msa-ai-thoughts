import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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

const mockThought: Thought = {
  id: '123e4567-e89b-12d3-a456-426614174000',
  content: 'This is a positive test thought',
  thumbsUp: 5,
  thumbsDown: 2,
  status: ThoughtStatus.APPROVED,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
  author: 'Test Author',
  authorBio: 'Test Bio',
};

const mockThought2: Thought = {
  id: '987e6543-e89b-12d3-a456-426614174001',
  content: 'This is another positive test thought',
  thumbsUp: 3,
  thumbsDown: 1,
  status: ThoughtStatus.APPROVED,
  createdAt: '2024-01-02T00:00:00Z',
  updatedAt: '2024-01-02T00:00:00Z',
  author: 'Another Author',
  authorBio: 'Another Bio',
};

describe('Random Thought Display Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('fetches and displays a random thought on page load', async () => {
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThought);

    render(<Home />);

    // Should show loading state initially
    expect(screen.getByTestId('thought-skeleton')).toBeInTheDocument();

    // Wait for thought to load and display
    await waitFor(() => {
      expect(screen.getByText(mockThought.content)).toBeInTheDocument();
    });

    // Verify API was called
    expect(apiClient.getRandomThought).toHaveBeenCalledTimes(1);
  });

  test('thumbs up button updates UI state with visual feedback', async () => {
    const updatedThought = { ...mockThought, thumbsUp: 6 };
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThought);
    (apiClient.thumbsUpThought as jest.Mock).mockResolvedValue(updatedThought);

    const user = userEvent.setup();
    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThought.content)).toBeInTheDocument();
    });

    // Find and click thumbs up button
    const thumbsUpButton = screen.getByRole('button', { name: /thumbs up/i });
    await user.click(thumbsUpButton);

    // Verify API was called with correct ID
    await waitFor(() => {
      expect(apiClient.thumbsUpThought).toHaveBeenCalledWith(mockThought.id);
    });

    // Verify visual feedback: thumbs up button should be solid/filled
    expect(thumbsUpButton).toHaveClass('bg-green-600');

    // Verify thumbs down button is greyed out/disabled
    const thumbsDownButton = screen.getByRole('button', { name: /thumbs down/i });
    expect(thumbsDownButton).toBeDisabled();
    expect(thumbsDownButton).toHaveClass('opacity-50');
  });

  test('thumbs down button updates UI state with visual feedback', async () => {
    const updatedThought = { ...mockThought, thumbsDown: 3 };
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThought);
    (apiClient.thumbsDownThought as jest.Mock).mockResolvedValue(updatedThought);

    const user = userEvent.setup();
    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThought.content)).toBeInTheDocument();
    });

    // Find and click thumbs down button
    const thumbsDownButton = screen.getByRole('button', { name: /thumbs down/i });
    await user.click(thumbsDownButton);

    // Verify API was called with correct ID
    await waitFor(() => {
      expect(apiClient.thumbsDownThought).toHaveBeenCalledWith(mockThought.id);
    });

    // Verify visual feedback: thumbs down button should be solid/filled
    expect(thumbsDownButton).toHaveClass('bg-red-600');

    // Verify thumbs up button is greyed out/disabled
    const thumbsUpButton = screen.getByRole('button', { name: /thumbs up/i });
    expect(thumbsUpButton).toBeDisabled();
    expect(thumbsUpButton).toHaveClass('opacity-50');
  });

  test('View Another Thought button fetches new thought', async () => {
    (apiClient.getRandomThought as jest.Mock)
      .mockResolvedValueOnce(mockThought)
      .mockResolvedValueOnce(mockThought2);

    const user = userEvent.setup();
    render(<Home />);

    // Wait for first thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThought.content)).toBeInTheDocument();
    });

    // Find and click "View Another Thought" button
    const viewAnotherButton = screen.getByRole('button', { name: /view another thought/i });
    await user.click(viewAnotherButton);

    // Wait for new thought to display
    await waitFor(() => {
      expect(screen.getByText(mockThought2.content)).toBeInTheDocument();
    });

    // Verify API was called twice (initial load + view another)
    expect(apiClient.getRandomThought).toHaveBeenCalledTimes(2);

    // Verify first thought is no longer displayed
    expect(screen.queryByText(mockThought.content)).not.toBeInTheDocument();
  });

  test('rating buttons are disabled after selection', async () => {
    const updatedThought = { ...mockThought, thumbsUp: 6 };
    (apiClient.getRandomThought as jest.Mock).mockResolvedValue(mockThought);
    (apiClient.thumbsUpThought as jest.Mock).mockResolvedValue(updatedThought);

    const user = userEvent.setup();
    render(<Home />);

    // Wait for thought to load
    await waitFor(() => {
      expect(screen.getByText(mockThought.content)).toBeInTheDocument();
    });

    const thumbsUpButton = screen.getByRole('button', { name: /thumbs up/i });
    const thumbsDownButton = screen.getByRole('button', { name: /thumbs down/i });

    // Initially, both buttons should be enabled
    expect(thumbsUpButton).not.toBeDisabled();
    expect(thumbsDownButton).not.toBeDisabled();

    // Click thumbs up
    await user.click(thumbsUpButton);

    // After rating, both buttons should be disabled
    await waitFor(() => {
      expect(thumbsUpButton).toBeDisabled();
      expect(thumbsDownButton).toBeDisabled();
    });
  });
});
