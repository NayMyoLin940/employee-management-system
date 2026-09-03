import axios from 'axios'

const apiOrigin = (
  import.meta.env.VITE_API_BASE_URL || ''
).replace(/\/$/, '')

const client = axios.create({
  baseURL: `${apiOrigin}/api`,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
})

export const employeeApi = {
  getAll: async () => (await client.get('/employees')).data,

  getById: async (id) =>
    (await client.get(`/employees/${id}`)).data,

  create: async (employee) =>
    (await client.post('/employees', employee)).data,

  update: async (id, employee) =>
    (await client.put(`/employees/${id}`, employee)).data,

  remove: async (id) =>
    client.delete(`/employees/${id}`),
}

export function getApiError(error) {
  if (!error.response) {
    return {
      message:
        'Cannot connect to the server. Check that the backend is running.',
      fields: {},
      status: 0,
    }
  }

  const { status, data } = error.response

  const fallback = {
    400: 'Please review the highlighted fields.',
    404: 'The requested employee could not be found.',
    409: 'This email address is already in use.',
  }

  return {
    message:
      data?.message ||
      fallback[status] ||
      'Something went wrong. Please try again.',
    fields: data?.fields || {},
    status,
  }
}