'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { apiClient, Thought, ThoughtStatus } from '@/lib/api-client';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { ThumbsUp, ThumbsDown } from 'lucide-react';

const thoughtSchema = z.object({
  content: z
    .string()
    .min(10, 'Content must be at least 10 characters')
    .max(500, 'Content must not exceed 500 characters'),
  author: z
    .string()
    .max(200, 'Author must not exceed 200 characters')
    .optional(),
  authorBio: z
    .string()
    .max(200, 'Author Bio must not exceed 200 characters')
    .optional(),
  status: z.nativeEnum(ThoughtStatus),
});

type ThoughtFormValues = z.infer<typeof thoughtSchema>;

export default function EditThoughtPage() {
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [thought, setThought] = useState<Thought | null>(null);
  const router = useRouter();
  const params = useParams();
  const { toast } = useToast();
  const thoughtId = params.id as string;

  const form = useForm<ThoughtFormValues>({
    resolver: zodResolver(thoughtSchema),
    defaultValues: {
      content: '',
      author: '',
      authorBio: '',
      status: ThoughtStatus.IN_REVIEW,
    },
  });

  useEffect(() => {
    loadThought();
  }, [thoughtId]);

  async function loadThought() {
    try {
      setIsLoading(true);
      const data = await apiClient.getThoughtById(thoughtId);
      setThought(data);
      form.reset({
        content: data.content,
        author: data.author || '',
        authorBio: data.authorBio || '',
        status: data.status,
      });
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.status === 404
          ? 'Thought not found.'
          : 'Failed to load thought. Please try again.',
        variant: 'destructive',
      });
      router.push('/thoughts');
    } finally {
      setIsLoading(false);
    }
  }

  async function onSubmit(values: ThoughtFormValues) {
    try {
      setIsSubmitting(true);
      await apiClient.updateThought(thoughtId, {
        content: values.content,
        author: values.author,
        authorBio: values.authorBio,
        status: values.status,
      });
      toast({
        title: 'Success',
        description: 'Thought updated successfully.',
      });
      router.push('/thoughts');
    } catch (error: any) {
      let errorMessage = 'Failed to update thought. Please try again.';

      if (error.status === 404) {
        errorMessage = 'Thought not found.';
      } else if (error.status === 400) {
        errorMessage = error.message || 'Invalid input. Please check your data.';
      }

      toast({
        title: 'Error',
        description: errorMessage,
        variant: 'destructive',
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  function calculateRatingPercentage() {
    if (!thought) return { percentage: 0, dominant: 'none' };

    const total = thought.thumbsUp + thought.thumbsDown;
    if (total === 0) return { percentage: 0, dominant: 'none' };

    const maxVotes = Math.max(thought.thumbsUp, thought.thumbsDown);
    const percentage = (maxVotes / total) * 100;
    const dominant = thought.thumbsUp > thought.thumbsDown ? 'up' : 'down';

    return { percentage, dominant };
  }

  if (isLoading) {
    return (
      <div className="container mx-auto py-10 max-w-2xl">
        <div className="p-8 text-center text-gray-500">Loading thought...</div>
      </div>
    );
  }

  if (!thought) {
    return null;
  }

  const contentLength = form.watch('content').length;
  const authorLength = form.watch('author')?.length || 0;
  const authorBioLength = form.watch('authorBio')?.length || 0;
  const rating = calculateRatingPercentage();

  return (
    <div className="container mx-auto py-10 max-w-2xl">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">Edit Thought</h1>
        <p className="text-gray-500 mt-2">
          Update the content and status of this thought.
        </p>
      </div>

      {/* Rating Statistics */}
      <div className="mb-6 p-4 bg-gray-50 rounded-lg">
        <h2 className="text-sm font-semibold text-gray-700 mb-3">Rating Statistics</h2>
        <div className="grid grid-cols-3 gap-4">
          <div className="flex items-center gap-2">
            <ThumbsUp className="h-5 w-5 text-green-600" />
            <div>
              <div className="text-2xl font-bold">{thought.thumbsUp}</div>
              <div className="text-xs text-gray-500">Thumbs Up</div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <ThumbsDown className="h-5 w-5 text-red-600" />
            <div>
              <div className="text-2xl font-bold">{thought.thumbsDown}</div>
              <div className="text-xs text-gray-500">Thumbs Down</div>
            </div>
          </div>
          <div>
            <div className="text-2xl font-bold">
              {rating.dominant === 'none' ? '-' : `${rating.percentage.toFixed(1)}%`}
            </div>
            <div className="text-xs text-gray-500">
              {rating.dominant === 'none'
                ? 'No ratings'
                : rating.dominant === 'up'
                ? 'Positive Rating'
                : 'Negative Rating'}
            </div>
          </div>
        </div>
      </div>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          <FormField
            control={form.control}
            name="content"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Content</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Enter thought content..."
                    className="min-h-[150px]"
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  {contentLength} / 500 characters (minimum 10 required)
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="author"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Author</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Enter author name (optional)"
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  {authorLength} / 200 characters
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="authorBio"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Author Bio</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Enter author bio (optional)"
                    className="min-h-[80px]"
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  {authorBioLength} / 200 characters
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="status"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Status</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select a status" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value={ThoughtStatus.APPROVED}>APPROVED</SelectItem>
                    <SelectItem value={ThoughtStatus.IN_REVIEW}>IN_REVIEW</SelectItem>
                    <SelectItem value={ThoughtStatus.REMOVED}>REMOVED</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription>
                  Change the thought status to control its visibility and state.
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <div className="flex gap-4">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Updating...' : 'Update Thought'}
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push('/thoughts')}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
}

export { EditThoughtPage };
