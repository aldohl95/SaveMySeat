import { Link } from 'react-router-dom';
import { AlertIcon } from './icons';
import { EmptyState } from './ui';

function NotFoundPage() {
  return (
    <EmptyState icon={<AlertIcon size={22} />} title="Page not found">
      <p>The page you're looking for doesn't exist.</p>
      <Link to="/events" className="btn btn-secondary" style={{ marginTop: 8 }}>
        Browse events
      </Link>
    </EmptyState>
  );
}

export default NotFoundPage;
