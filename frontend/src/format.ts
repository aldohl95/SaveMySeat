const currency = new Intl.NumberFormat(undefined, {
  style: 'currency',
  currency: 'USD',
});

const dayFormat = new Intl.DateTimeFormat(undefined, {
  weekday: 'short',
  month: 'short',
  day: 'numeric',
  year: 'numeric',
});

const timeFormat = new Intl.DateTimeFormat(undefined, {
  hour: 'numeric',
  minute: '2-digit',
});

const shortDateTimeFormat = new Intl.DateTimeFormat(undefined, {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
  hour: 'numeric',
  minute: '2-digit',
});

export function formatPrice(cents: number): string {
  return currency.format(cents / 100);
}

export function formatDay(iso: string): string {
  return dayFormat.format(new Date(iso));
}

export function formatTime(iso: string): string {
  return timeFormat.format(new Date(iso));
}

export function formatTimeRange(startIso: string, endIso: string): string {
  return `${formatTime(startIso)} – ${formatTime(endIso)}`;
}

export function formatDateTime(iso: string): string {
  return shortDateTimeFormat.format(new Date(iso));
}

export function dateParts(iso: string): { month: string; day: string } {
  const date = new Date(iso);
  return {
    month: date.toLocaleString(undefined, { month: 'short' }),
    day: String(date.getDate()),
  };
}

export function formatCountdown(ms: number): string {
  const totalSeconds = Math.max(0, Math.ceil(ms / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

export function pluralize(count: number, word: string): string {
  return `${count} ${word}${count === 1 ? '' : 's'}`;
}
