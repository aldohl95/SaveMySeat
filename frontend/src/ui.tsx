import type { ReactNode } from 'react';
import { AlertIcon } from './icons';

export function Loading({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="state" role="status">
      <div className="spinner" />
      <span>{label}…</span>
    </div>
  );
}

export function ErrorState({ message }: { message: string }) {
  return (
    <div className="state" role="alert">
      <div className="state-icon">
        <AlertIcon size={22} />
      </div>
      <h2>Something went wrong</h2>
      <p>{message}</p>
    </div>
  );
}

type EmptyStateProps = {
  icon: ReactNode;
  title: string;
  children?: ReactNode;
};

export function EmptyState({ icon, title, children }: EmptyStateProps) {
  return (
    <div className="state">
      <div className="state-icon">{icon}</div>
      <h2>{title}</h2>
      {children}
    </div>
  );
}

export function ErrorAlert({ message }: { message: string }) {
  return (
    <div className="alert alert-error" role="alert">
      <AlertIcon />
      <span>{message}</span>
    </div>
  );
}
