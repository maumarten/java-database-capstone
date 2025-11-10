// Create an application-scoped user with readWrite on the prescriptions database
db = db.getSiblingDB('prescriptions');
db.createUser({
  user: 'cms_app',
  pwd: 'cms_app_pw',
  roles: [{ role: 'readWrite', db: 'prescriptions' }],
});


