import type { UpdateUserRequest, User, SubscriptionRequest, SubscriptionResponse, SubscriptionPlanResponse } from "../models/User";
import { USER_SERVICE_URL } from "./ApiDictionary";
import { client } from "./AxiosConfig";

export const UserAPI = {
    findByGuid: (guid: string) => client.get<User>(`${USER_SERVICE_URL}/users/${guid}`),

    updateByGuid: (guid: string, data: UpdateUserRequest) => client.put<User>(`${USER_SERVICE_URL}/users/${guid}`, data),

    getBalance: (guid: string) => client.get<number>(`${USER_SERVICE_URL}/users/balance/${guid}`),

    purchase: (data: SubscriptionRequest) => client.post<SubscriptionResponse>(`${USER_SERVICE_URL}/user-subscriptions/purchase`, data),

    getSubscription: () => client.get<SubscriptionResponse>(`${USER_SERVICE_URL}/user-subscriptions`),

    getSubscriptionPlans: () => client.get<SubscriptionPlanResponse[]>(`${USER_SERVICE_URL}/subscription-plans/all`),
};
