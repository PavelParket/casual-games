import type { UpdateUserRequest, User, SubscriptionRequest, SubscriptionResponse, SubscriptionPlanResponse } from "../models/User";
import { USER_SERVICE_URL } from "./ApiDictionary";
import { client } from "./AxiosConfig";

export const UserAPI = {
    findByGuid: (guid: string) => client.get<User>(`${USER_SERVICE_URL}/users/${guid}`),

    updateByGuid: (guid: string, data: UpdateUserRequest) => client.put<User>(`${USER_SERVICE_URL}/users/${guid}`, data),

    getBalance: (guid: string) => client.get<number>(`${USER_SERVICE_URL}/users/balance/${guid}`),

    uploadProfilePicture: (guid: string, files: { full: File; mini: File }) => {
        const formData = new FormData();
        formData.append("full", files.full);
        formData.append("mini", files.mini);

        return client.post<User>(`${USER_SERVICE_URL}/users/attachments/${guid}`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    deleteProfilePicture: (guid: string) => client.delete(`${USER_SERVICE_URL}/users/attachments/${guid}`),

    purchase: (data: SubscriptionRequest) => client.post<SubscriptionResponse>(`${USER_SERVICE_URL}/user-subscriptions/purchase`, data),

    getSubscription: () => client.get<SubscriptionResponse>(`${USER_SERVICE_URL}/user-subscriptions`),

    getSubscriptionPlans: () => client.get<SubscriptionPlanResponse[]>(`${USER_SERVICE_URL}/subscription-plans`),
};
