module.exports = function (grunt) {
  grunt.initConfig({
    watch: {
      data: {
        files: ['src/**'],
        tasks: ['copy']
      }
    },
    copy: {
      main: {
        expand: true,
        cwd: 'src/',
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
        port: 8080,
        middleware: [require('http-proxy-middleware')('/api', {target: 'http://dashboard.kuona.io:9001'})]
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
    localBrowserSync: {
      options: {
        port: 8080,
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
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  require('load-grunt-tasks')(grunt);

  grunt.registerTask('build', ['clean', 'copy']);
  grunt.registerTask('dev', ['build', 'browserSync', 'watch']);
  grunt.registerTask('dev-local', ['build', 'localBrowserSync', 'watch']);
  grunt.registerTask('default', ['build']);
};
