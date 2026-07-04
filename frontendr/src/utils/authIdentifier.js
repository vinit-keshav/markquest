export const getIdentifierPayload = (identifier) => {
  const value = identifier.trim();
  return value.includes("@") ? { email: value } : { mobile: value };
};
