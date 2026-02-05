// TypeScript interfaces matching backend Thought entity schema

export enum ThoughtStatus {
  APPROVED = 'APPROVED',
  REMOVED = 'REMOVED',
  IN_REVIEW = 'IN_REVIEW'
}

export interface Thought {
  id: string;
  content: string;
  thumbsUp: number;
  thumbsDown: number;
  status: ThoughtStatus;
  createdAt: string;
  updatedAt: string;
  author: string;
  authorBio: string;
}
