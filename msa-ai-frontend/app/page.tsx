'use client';

import { useEffect, useState } from 'react';
import { ThumbsUp, ThumbsDown } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { apiClient, ApiError } from '@/lib/api-client';
import { Thought } from '@/lib/types';

type RatingState = 'up' | 'down' | null;

export default function Home() {
  const [thought, setThought] = useState<Thought | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [rating, setRating] = useState<RatingState>(null);
  const [ratingLoading, setRatingLoading] = useState<boolean>(false);

  // Fetch random thought
  const fetchRandomThought = async () => {
    setLoading(true);
    setRating(null); // Reset rating state
    try {
      const data = await apiClient.getRandomThought();
      setThought(data);
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        toast.error('No thoughts available at this time. Please try again later.');
      } else {
        toast.error('Failed to load thought. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Load initial thought on mount
  useEffect(() => {
    fetchRandomThought();
  }, []);

  // Handle thumbs up
  const handleThumbsUp = async () => {
    if (!thought || rating !== null) return;

    setRatingLoading(true);
    // Optimistic UI update
    setRating('up');

    try {
      await apiClient.thumbsUpThought(thought.id);
    } catch (error) {
      toast.error('Failed to submit rating. Please try again.');
      // Revert optimistic update on error
      setRating(null);
    } finally {
      setRatingLoading(false);
    }
  };

  // Handle thumbs down
  const handleThumbsDown = async () => {
    if (!thought || rating !== null) return;

    setRatingLoading(true);
    // Optimistic UI update
    setRating('down');

    try {
      await apiClient.thumbsDownThought(thought.id);
    } catch (error) {
      toast.error('Failed to submit rating. Please try again.');
      // Revert optimistic update on error
      setRating(null);
    } finally {
      setRatingLoading(false);
    }
  };

  // Handle view another thought
  const handleViewAnother = () => {
    fetchRandomThought();
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 via-white to-purple-50 dark:from-zinc-900 dark:via-black dark:to-zinc-900 px-4 py-8">
      <main className="flex w-full max-w-3xl flex-col items-center justify-center gap-8">
        {/* Thought Content */}
        <div className="w-full rounded-2xl bg-white dark:bg-zinc-800 p-8 md:p-12 shadow-xl">
          {loading ? (
            <div data-testid="thought-skeleton" className="space-y-4">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-5/6" />
              <Skeleton className="h-8 w-4/6" />
              <Skeleton className="h-4 w-3/6 mt-6" />
            </div>
          ) : thought ? (
            <div className="space-y-4">
              <p className="text-2xl md:text-3xl lg:text-4xl font-medium leading-relaxed text-center text-zinc-800 dark:text-zinc-100">
                {thought.content}
              </p>
              <p className="text-sm text-center text-zinc-600 dark:text-zinc-400 mt-6">
                {thought.author}, {thought.authorBio}
              </p>
            </div>
          ) : (
            <p className="text-2xl md:text-3xl text-center text-zinc-500 dark:text-zinc-400">
              No thoughts available
            </p>
          )}
        </div>

        {/* Rating Buttons */}
        {thought && !loading && (
          <div className="flex gap-4 items-center justify-center">
            <Button
              onClick={handleThumbsUp}
              disabled={rating !== null || ratingLoading}
              aria-label="Thumbs up"
              size="lg"
              className={`
                h-14 w-14 md:h-16 md:w-16 rounded-full transition-all duration-300 transform hover:scale-110
                ${
                  rating === 'up'
                    ? 'bg-green-600 hover:bg-green-600 text-white shadow-lg shadow-green-300'
                    : rating === 'down'
                    ? 'opacity-50 cursor-not-allowed bg-zinc-200 hover:bg-zinc-200'
                    : 'bg-zinc-100 hover:bg-green-50 text-zinc-700 hover:text-green-600'
                }
              `}
            >
              <ThumbsUp className="h-6 w-6 md:h-7 md:w-7" />
            </Button>

            <Button
              onClick={handleThumbsDown}
              disabled={rating !== null || ratingLoading}
              aria-label="Thumbs down"
              size="lg"
              className={`
                h-14 w-14 md:h-16 md:w-16 rounded-full transition-all duration-300 transform hover:scale-110
                ${
                  rating === 'down'
                    ? 'bg-red-600 hover:bg-red-600 text-white shadow-lg shadow-red-300'
                    : rating === 'up'
                    ? 'opacity-50 cursor-not-allowed bg-zinc-200 hover:bg-zinc-200'
                    : 'bg-zinc-100 hover:bg-red-50 text-zinc-700 hover:text-red-600'
                }
              `}
            >
              <ThumbsDown className="h-6 w-6 md:h-7 md:w-7" />
            </Button>
          </div>
        )}

        {/* View Another Thought Button */}
        {thought && !loading && (
          <Button
            onClick={handleViewAnother}
            disabled={loading}
            aria-label="View another thought"
            size="lg"
            className="px-8 py-6 text-lg font-medium bg-blue-600 hover:bg-blue-700 text-white rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
          >
            View Another Thought
          </Button>
        )}

        {/* Retry Button (shown when no thought and not loading) */}
        {!thought && !loading && (
          <Button
            onClick={handleViewAnother}
            size="lg"
            className="px-8 py-6 text-lg font-medium bg-blue-600 hover:bg-blue-700 text-white rounded-full shadow-lg"
          >
            Try Again
          </Button>
        )}
      </main>
    </div>
  );
}
