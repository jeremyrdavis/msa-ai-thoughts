export enum ThoughtStatus {
  APPROVED = "APPROVED",
  REMOVED = "REMOVED",
  IN_REVIEW = "IN_REVIEW",
}

export interface Thought {
  id: string;
  content: string;
  author: string;
  authorBio: string;
  thumbsUp: number;
  thumbsDown: number;
  status: ThoughtStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateThoughtRequest {
  content: string;
  author?: string;
  authorBio?: string;
}

export interface UpdateThoughtRequest {
  content: string;
  author?: string;
  authorBio?: string;
  status: ThoughtStatus;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public details?: unknown
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = `HTTP error ${response.status}`;
    let errorDetails;

    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorData.error || errorMessage;
      errorDetails = errorData;
    } catch {
      // If response is not JSON, use status text
      errorMessage = response.statusText || errorMessage;
    }

    throw new ApiError(response.status, errorMessage, errorDetails);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const apiClient = {
  async getThoughts(page = 0, size = 20): Promise<Thought[]> {
    const response = await fetch(
      `${API_BASE_URL}/thoughts?page=${page}&size=${size}`
    );
    return handleResponse<Thought[]>(response);
  },

  async getThoughtById(id: string): Promise<Thought> {
    const response = await fetch(`${API_BASE_URL}/thoughts/${id}`);
    return handleResponse<Thought>(response);
  },

  async createThought(request: CreateThoughtRequest): Promise<Thought> {
    const response = await fetch(`${API_BASE_URL}/thoughts`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });
    return handleResponse<Thought>(response);
  },

  async updateThought(
    id: string,
    request: UpdateThoughtRequest
  ): Promise<Thought> {
    const response = await fetch(`${API_BASE_URL}/thoughts/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });
    return handleResponse<Thought>(response);
  },

  async deleteThought(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/thoughts/${id}`, {
      method: "DELETE",
    });
    return handleResponse<void>(response);
  },
};

export { ApiError };
