export interface FAQItemData {
    question: string;
    answer: string;
}

export const UPGRADE_FAQ: FAQItemData[] = [
    {
        question: "How does the billing cycle work?",
        answer: "All premium statuses (PRO and VIP) are billed on a 30-day cycle. By default, your subscription will automatically renew at the end of this period to ensure uninterrupted access to your benefits."
    },
    {
        question: "What if I decide to upgrade from PRO to VIP?",
        answer: "If you upgrade to VIP while having an active PRO status, you will receive a dynamic discount. This discount is calculated based on the remaining days of your PRO subscription, ensuring you get full value for your previous purchase."
    },
    {
        question: "Can I downgrade my status?",
        answer: "Yes! If you choose to downgrade, your current premium benefits will remain active until the end of your 30-day billing cycle. The new status, along with any applicable charges, will only take effect once the current cycle expires."
    },
    {
        question: "What happens if I don't have enough balance for renewal?",
        answer: "If your CG Coins balance is insufficient when it's time to renew, your subscription will simply expire, and your account will safely revert to the Default status. You can upgrade again at any time."
    },
];
