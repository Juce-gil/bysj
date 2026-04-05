export const formatPrice = (price: number) => `¥ ${price.toFixed(2)}`;

export const formatDate = (value: string) => value.replace('T', ' ');

export const maskSchool = (value: string) => `来自 ${value}`;
