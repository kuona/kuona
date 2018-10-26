module.exports = function (grunt) {
  grunt.initConfig({
    "browserify": {
      admin: {
        src: ['src/admin/**/*.js'],
        dest: 'build/app.js',
      }
    },
    "babel": {
      options: {
        sourceMap: true
      },
      dist: {
        files: [
          {
            expand: true,
            cwd: 'src/js',
            src: ['**/*.js'],
            dest: 'out/js'
          }
        ]
      }
    },
    watch: {
      data: {
        files: ['src/**', 'lib/**', '!src/sass'],
        tasks: ['copy']
      },
      sass: {
        files: ['src/sass/**'],
        tasks: ['compass']
      }
    },
    copy: {
      main: {
        expand: true,
        cwd: 'src/',
        src: '**',
        dest: 'out/',
      },
      lib: {
        expand: true,
        cwd: 'lib/',
        src: '**',
        dest: 'out/',
      }
    },
    clean: {
      build: {
        src: ['out']
      }
    },
    browserSync: {
      options: {
        port: 4000,
        middleware: [require('http-proxy-middleware')('/api', {target: 'http://localhost:9001'})]
      },
      dev: {
        bsFiles: {
          src: [
            'out/**'
          ]
        },
        options: {
          watchTask: true,
          server: './out'
        }
      }
    },
    compass: {                  // Task
      dist: {                   // Target
        options: {              // Target options
          sassDir: 'src/sass',
          cssDir: 'out/css',
          environment: 'production'
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  require('load-grunt-tasks')(grunt);

//  grunt.registerTask("default", ["babel"]);
  grunt.registerTask('build', ['clean', 'copy', 'compass', 'babel']);
  grunt.registerTask('dev', ['build', 'browserSync', 'watch']);
  grunt.registerTask('default', ['browserify:admin', 'build']);
};
