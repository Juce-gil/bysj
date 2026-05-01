import { describe, it, expect } from 'vitest';
import { formatPrice } from '@/utils/format';

describe('formatPrice', () => {
  it('should format price correctly', () => {
    expect(formatPrice(100)).toBe('¥100.00');
    expect(formatPrice(99.9)).toBe('¥99.90');
    expect(formatPrice(0)).toBe('¥0.00');
  });

  it('should handle large numbers', () => {
    expect(formatPrice(10000)).toBe('¥10000.00');
    expect(formatPrice(999999.99)).toBe('¥999999.99');
  });

  it('should handle decimal places', () => {
    expect(formatPrice(12.345)).toBe('¥12.35');
    expect(formatPrice(12.344)).toBe('¥12.34');
  });
});
