export const buildCoverBackground = (fallback: string, imageUrl?: string | null): string => {
  if (!imageUrl) {
    return fallback;
  }

  const safeUrl = imageUrl.replace(/"/g, '%22');
  return `linear-gradient(180deg, rgba(8, 16, 32, 0.06) 0%, rgba(8, 16, 32, 0.48) 100%), url("${safeUrl}") center/cover no-repeat`;
};
