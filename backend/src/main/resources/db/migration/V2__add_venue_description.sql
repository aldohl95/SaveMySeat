-- Adds an optional description field to venues for UI display
-- nullable because existing rows have no natural default value
ALTER TABLE venues ADD COLUMN description TEXT;