flow: Code → Deploy

Demo: 

if (flags.isEnabled('new-payment-validation', user)) {
  return validatePaymentV2(order);
}
return validatePaymentLegacy(order);
